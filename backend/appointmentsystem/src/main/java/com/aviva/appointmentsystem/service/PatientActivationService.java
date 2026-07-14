package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.LoginResponse;
import com.aviva.appointmentsystem.dto.PatientActivationCompleteRequest;
import com.aviva.appointmentsystem.dto.PatientActivationRequest;
import com.aviva.appointmentsystem.dto.PatientActivationResponse;
import com.aviva.appointmentsystem.dto.PatientActivationStep;
import com.aviva.appointmentsystem.dto.PatientActivationVerifyCodeRequest;
import com.aviva.appointmentsystem.dto.PatientActivationVerifyCodeResponse;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.PatientActivationChallenge;
import com.aviva.appointmentsystem.entity.Role;
import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.ActivationRequestLimitedException;
import com.aviva.appointmentsystem.exception.InvalidActivationCodeException;
import com.aviva.appointmentsystem.exception.ResourceAlreadyExistsException;
import com.aviva.appointmentsystem.repository.PatientActivationChallengeRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
import com.aviva.appointmentsystem.repository.UserRepository;
import com.aviva.appointmentsystem.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Activa el acceso al portal de un paciente previamente creado por el staff.
 * El DNI identifica el perfil, pero la vinculacion solo se completa despues
 * de demostrar acceso al correo que ya pertenece a ese perfil.
 */
@Service
public class PatientActivationService {

    private static final Logger logger =
            LoggerFactory.getLogger(PatientActivationService.class);

    static final int OTP_LENGTH = 6;
    static final int ACTIVATION_TOKEN_BYTES = 32;
    static final int MAX_FAILED_ATTEMPTS = 5;
    static final int MAX_REQUESTS_PER_HOUR = 5;
    static final long OTP_TTL_MINUTES = 10;
    static final long ACTIVATION_TOKEN_TTL_MINUTES = 5;
    static final long REQUEST_COOLDOWN_SECONDS = 60;

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientActivationChallengeRepository challengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailSender emailSender;
    private final EmailTemplateService emailTemplateService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PatientActivationService(
            PatientRepository patientRepository,
            UserRepository userRepository,
            PatientActivationChallengeRepository challengeRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            EmailSender emailSender,
            EmailTemplateService emailTemplateService
    ) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailSender = emailSender;
        this.emailTemplateService = emailTemplateService;
    }

    /**
     * Determina el flujo por DNI y, cuando corresponde, envia un OTP al correo
     * que ya esta registrado. Nunca acepta un correo indicado por el visitante.
     */
    @Transactional
    public PatientActivationResponse requestActivation(PatientActivationRequest request) {
        String dni = request.dni().trim();
        Patient patient = patientRepository.findByDniForUpdate(dni).orElse(null);

        if (patient == null) {
            return response(PatientActivationStep.NEW_PATIENT, null);
        }

        if (patient.getUser() != null) {
            return response(PatientActivationStep.ACCOUNT_EXISTS, null);
        }

        if (patient.getStatus() != UserStatus.ACTIVE || isBlank(patient.getEmail())) {
            return response(PatientActivationStep.CONTACT_STAFF, null);
        }

        LocalDateTime now = LocalDateTime.now();
        enforceRequestLimits(patient, now);
        consumePreviousChallenges(patient, now);

        String otp = generateOtp();
        PatientActivationChallenge challenge = new PatientActivationChallenge();
        challenge.setId(UUID.randomUUID());
        challenge.setPatient(patient);
        challenge.setOtpHash(passwordEncoder.encode(otp));
        challenge.setFailedAttempts(0);
        challenge.setCreatedAt(now);
        challenge.setExpiresAt(now.plusMinutes(OTP_TTL_MINUTES));
        challenge.setConsumedAt(null);
        challenge.setActivationTokenHash(null);
        challenge.setCodeVerifiedAt(null);
        challenge.setActivationTokenExpiresAt(null);

        challengeRepository.saveAndFlush(challenge);

        EmailContent email = emailTemplateService.patientActivation(
                patient,
                otp,
                OTP_TTL_MINUTES
        );
        emailSender.send(
                patient.getEmail(),
                email.subject(),
                email.textContent(),
                email.htmlContent()
        );

        logger.info("Desafio de activacion creado para patientId={}", patient.getId());
        return response(PatientActivationStep.VERIFICATION_REQUIRED, challenge.getId());
    }

    /** Verifica el OTP y emite un token temporal, sin crear aun credenciales. */
    @Transactional(noRollbackFor = InvalidActivationCodeException.class)
    public PatientActivationVerifyCodeResponse verifyCode(
            PatientActivationVerifyCodeRequest request
    ) {
        LocalDateTime now = LocalDateTime.now();
        LockedActivation locked = loadLockedActivation(request.challengeId());
        Patient patient = locked.patient();
        PatientActivationChallenge challenge = locked.challenge();

        validateUsableOtpChallenge(challenge, now);
        if (patient.getStatus() != UserStatus.ACTIVE || patient.getUser() != null) {
            consumeChallenge(challenge, now);
            throw new InvalidActivationCodeException();
        }

        if (!passwordEncoder.matches(request.code(), challenge.getOtpHash())) {
            registerFailedAttempt(challenge, now);
            throw new InvalidActivationCodeException();
        }

        String activationToken = generateActivationToken();
        LocalDateTime tokenExpiresAt = now.plusMinutes(ACTIVATION_TOKEN_TTL_MINUTES);
        challenge.setActivationTokenHash(passwordEncoder.encode(activationToken));
        challenge.setCodeVerifiedAt(now);
        challenge.setActivationTokenExpiresAt(tokenExpiresAt);
        challengeRepository.save(challenge);

        logger.info("Codigo de activacion verificado para patientId={}", patient.getId());
        return new PatientActivationVerifyCodeResponse(
            challenge.getId(),
            activationToken,
            ACTIVATION_TOKEN_TTL_MINUTES * 60
        );
    }

    /**
     * Crea y vincula la cuenta solo si se presenta el token temporal emitido
     * por verify-code. El token nunca se recibe junto con el OTP.
     */
    @Transactional(noRollbackFor = InvalidActivationCodeException.class)
    public LoginResponse completeActivation(PatientActivationCompleteRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LockedActivation locked = loadLockedActivation(request.challengeId());
        Patient patient = locked.patient();
        PatientActivationChallenge challenge = locked.challenge();

        validateUsableActivationToken(challenge, now);
        if (patient.getStatus() != UserStatus.ACTIVE || patient.getUser() != null) {
            consumeChallenge(challenge, now);
            throw new InvalidActivationCodeException();
        }

        if (!passwordEncoder.matches(
                request.activationToken(),
                challenge.getActivationTokenHash()
        )) {
            throw new InvalidActivationCodeException();
        }

        String username = request.username().trim();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResourceAlreadyExistsException(
                "Ya existe un usuario con el nombre de usuario: " + username
            );
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(patient.getFirstName());
        user.setLastName(patient.getLastName());
        user.setRole(Role.PATIENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser;
        try {
            // El flush traduce tambien una carrera entre dos solicitudes que
            // escogieron el mismo username a un conflicto comprensible.
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceAlreadyExistsException(
                "Ya existe un usuario con el nombre de usuario: " + username
            );
        }
        patient.setUser(savedUser);
        patient.setUpdatedAt(now);
        patientRepository.save(patient);

        challenge.setConsumedAt(now);
        challenge.setActivationTokenHash(null);
        challengeRepository.save(challenge);

        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole().name());
        logger.info("Cuenta de portal activada para patientId={}", patient.getId());

        return new LoginResponse(
            token,
            savedUser.getRole().name(),
            savedUser.getUsername(),
            savedUser.getFirstName(),
            savedUser.getLastName()
        );
    }

    private void enforceRequestLimits(Patient patient, LocalDateTime now) {
        challengeRepository.findTopByPatientOrderByCreatedAtDesc(patient)
                .filter(latest -> latest.getCreatedAt().isAfter(
                    now.minusSeconds(REQUEST_COOLDOWN_SECONDS)
                ))
                .ifPresent(latest -> {
                    throw requestLimited();
                });

        long requestsLastHour = challengeRepository.countByPatientAndCreatedAtAfter(
            patient,
            now.minusHours(1)
        );
        if (requestsLastHour >= MAX_REQUESTS_PER_HOUR) {
            throw requestLimited();
        }
    }

    private void consumePreviousChallenges(Patient patient, LocalDateTime now) {
        List<PatientActivationChallenge> activeChallenges =
                challengeRepository.findByPatientAndConsumedAtIsNull(patient);
        if (activeChallenges.isEmpty()) {
            return;
        }

        activeChallenges.forEach(challenge -> {
            challenge.setConsumedAt(now);
            challenge.setActivationTokenHash(null);
        });
        challengeRepository.saveAll(activeChallenges);
    }

    private void validateUsableOtpChallenge(
            PatientActivationChallenge challenge,
            LocalDateTime now
    ) {
        // Un codigo ya verificado no puede emitir un segundo token. Tampoco se
        // invalida el token existente: complete puede seguir consumiendolo.
        if (challenge.getCodeVerifiedAt() != null
                || challenge.getActivationTokenHash() != null) {
            throw new InvalidActivationCodeException();
        }

        int failedAttempts = valueOrZero(challenge.getFailedAttempts());
        boolean unavailable = challenge.getConsumedAt() != null
                || !challenge.getExpiresAt().isAfter(now)
                || failedAttempts >= MAX_FAILED_ATTEMPTS;

        if (unavailable) {
            if (challenge.getConsumedAt() == null) {
                consumeChallenge(challenge, now);
            }
            throw new InvalidActivationCodeException();
        }
    }

    private void validateUsableActivationToken(
            PatientActivationChallenge challenge,
            LocalDateTime now
    ) {
        boolean unavailable = challenge.getConsumedAt() != null
                || challenge.getCodeVerifiedAt() == null
                || isBlank(challenge.getActivationTokenHash())
                || challenge.getActivationTokenExpiresAt() == null
                || !challenge.getActivationTokenExpiresAt().isAfter(now);

        if (unavailable) {
            if (challenge.getConsumedAt() == null
                    && challenge.getActivationTokenExpiresAt() != null
                    && !challenge.getActivationTokenExpiresAt().isAfter(now)) {
                consumeChallenge(challenge, now);
            }
            throw new InvalidActivationCodeException();
        }
    }

    private void registerFailedAttempt(
            PatientActivationChallenge challenge,
            LocalDateTime now
    ) {
        int failedAttempts = valueOrZero(challenge.getFailedAttempts()) + 1;
        challenge.setFailedAttempts(failedAttempts);
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            challenge.setConsumedAt(now);
        }
        challengeRepository.save(challenge);
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        return String.format("%0" + OTP_LENGTH + "d", secureRandom.nextInt(bound));
    }

    private String generateActivationToken() {
        byte[] tokenBytes = new byte[ACTIVATION_TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /** Mantiene el orden global de locks Patient -> Challenge. */
    private LockedActivation loadLockedActivation(UUID challengeId) {
        PatientActivationChallenge candidate = challengeRepository
                .findById(challengeId)
                .orElseThrow(InvalidActivationCodeException::new);
        Patient patient = patientRepository.findByIdForUpdate(candidate.getPatient().getId())
                .orElseThrow(InvalidActivationCodeException::new);
        PatientActivationChallenge challenge = challengeRepository
                .findByIdForUpdate(challengeId)
                .orElseThrow(InvalidActivationCodeException::new);

        if (!challenge.getPatient().getId().equals(patient.getId())) {
            throw new InvalidActivationCodeException();
        }
        return new LockedActivation(patient, challenge);
    }

    private void consumeChallenge(
            PatientActivationChallenge challenge,
            LocalDateTime now
    ) {
        challenge.setConsumedAt(now);
        challenge.setActivationTokenHash(null);
        challengeRepository.save(challenge);
    }

    private PatientActivationResponse response(
            PatientActivationStep nextStep,
            UUID challengeId
    ) {
        return new PatientActivationResponse(nextStep, challengeId);
    }

    private ActivationRequestLimitedException requestLimited() {
        return new ActivationRequestLimitedException();
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record LockedActivation(
            Patient patient,
            PatientActivationChallenge challenge
    ) {}
}
