package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.LoginResponse;
import com.aviva.appointmentsystem.dto.PatientActivationCompleteRequest;
import com.aviva.appointmentsystem.dto.PatientActivationRequest;
import com.aviva.appointmentsystem.dto.PatientActivationResponse;
import com.aviva.appointmentsystem.dto.PatientActivationStep;
import com.aviva.appointmentsystem.dto.PatientActivationVerifyCodeRequest;
import com.aviva.appointmentsystem.dto.PatientActivationVerifyCodeResponse;
import com.aviva.appointmentsystem.entity.Gender;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientActivationServiceTest {

    private static final Pattern OTP_PATTERN = Pattern.compile("\\b(\\d{6})\\b");
    private static final String ACTIVATION_TOKEN =
            "activation-token-with-at-least-32-characters-123";

    @Mock private PatientRepository patientRepository;
    @Mock private UserRepository userRepository;
    @Mock private PatientActivationChallengeRepository challengeRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private EmailSender emailSender;

    private PatientActivationService service;

    @BeforeEach
    void setUp() {
        EmailTemplateService emailTemplateService =
                new EmailTemplateService("http://localhost:5174");
        service = new PatientActivationService(
            patientRepository,
            userRepository,
            challengeRepository,
            passwordEncoder,
            jwtUtil,
            emailSender,
            emailTemplateService
        );
    }

    @Test
    void returnsNewPatientWithoutSendingEmailWhenDniDoesNotExist() {
        when(patientRepository.findByDniForUpdate("76543210"))
                .thenReturn(Optional.empty());

        PatientActivationResponse response = service.requestActivation(
            new PatientActivationRequest("76543210")
        );

        assertEquals(PatientActivationStep.NEW_PATIENT, response.nextStep());
        assertNull(response.challengeId());
        verify(emailSender, never()).send(
                anyString(), anyString(), anyString(), anyString());
        verify(challengeRepository, never()).saveAndFlush(any());
    }

    @Test
    void reportsExistingAccountWithoutSendingAnotherCode() {
        Patient patient = activePatient(41L);
        User existingUser = new User();
        existingUser.setUsername("maria.portal");
        patient.setUser(existingUser);
        when(patientRepository.findByDniForUpdate(patient.getDni()))
                .thenReturn(Optional.of(patient));

        PatientActivationResponse response = service.requestActivation(
            new PatientActivationRequest(patient.getDni())
        );

        assertEquals(PatientActivationStep.ACCOUNT_EXISTS, response.nextStep());
        assertNull(response.challengeId());
        verify(emailSender, never()).send(
                anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendsOtpToStoredEmailAndPersistsOnlyItsHash() {
        Patient patient = activePatient(41L);
        prepareAllowedRequest(patient);
        when(passwordEncoder.encode(anyString()))
                .thenAnswer(invocation -> "HASH[" + invocation.getArgument(0) + "]");
        when(challengeRepository.saveAndFlush(any(PatientActivationChallenge.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PatientActivationResponse response = service.requestActivation(
            new PatientActivationRequest(patient.getDni())
        );

        ArgumentCaptor<PatientActivationChallenge> challengeCaptor =
                ArgumentCaptor.forClass(PatientActivationChallenge.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(challengeRepository).saveAndFlush(challengeCaptor.capture());
        verify(emailSender).send(
            eq(patient.getEmail()),
            anyString(),
            messageCaptor.capture(),
            anyString()
        );

        String otp = extractOtp(messageCaptor.getValue());
        PatientActivationChallenge savedChallenge = challengeCaptor.getValue();
        assertEquals("HASH[" + otp + "]", savedChallenge.getOtpHash());
        assertNotEquals(otp, savedChallenge.getOtpHash());
        assertNull(savedChallenge.getActivationTokenHash());
        assertEquals(PatientActivationStep.VERIFICATION_REQUIRED, response.nextStep());
        assertEquals(savedChallenge.getId(), response.challengeId());
    }

    @Test
    void resendConsumesPreviousChallengeAndClearsItsTemporaryTokenHash() {
        Patient patient = activePatient(41L);
        PatientActivationChallenge previous = verifiedChallenge(patient);
        previous.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        when(patientRepository.findByDniForUpdate(patient.getDni()))
                .thenReturn(Optional.of(patient));
        when(challengeRepository.findTopByPatientOrderByCreatedAtDesc(patient))
                .thenReturn(Optional.of(previous));
        when(challengeRepository.countByPatientAndCreatedAtAfter(eq(patient), any()))
                .thenReturn(1L);
        when(challengeRepository.findByPatientAndConsumedAtIsNull(patient))
                .thenReturn(List.of(previous));
        when(passwordEncoder.encode(anyString())).thenReturn("new-otp-hash");

        service.requestActivation(new PatientActivationRequest(patient.getDni()));

        assertNotNull(previous.getConsumedAt());
        assertNull(previous.getActivationTokenHash());
        verify(challengeRepository).saveAll(List.of(previous));
        verify(emailSender).send(
                eq(patient.getEmail()), anyString(), anyString(), anyString());
    }

    @Test
    void rejectsCodeRequestsDuringCooldownWith429() {
        Patient patient = activePatient(41L);
        PatientActivationChallenge latest = challenge(patient, "hash");
        latest.setCreatedAt(LocalDateTime.now());
        when(patientRepository.findByDniForUpdate(patient.getDni()))
                .thenReturn(Optional.of(patient));
        when(challengeRepository.findTopByPatientOrderByCreatedAtDesc(patient))
                .thenReturn(Optional.of(latest));

        ActivationRequestLimitedException exception = assertThrows(
            ActivationRequestLimitedException.class,
            () -> service.requestActivation(new PatientActivationRequest(patient.getDni()))
        );

        assertEquals(429, exception.getStatusCode());
        verify(emailSender, never()).send(
                anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void rejectsMoreThanFiveCodeRequestsPerHourWith429() {
        Patient patient = activePatient(41L);
        when(patientRepository.findByDniForUpdate(patient.getDni()))
                .thenReturn(Optional.of(patient));
        when(challengeRepository.findTopByPatientOrderByCreatedAtDesc(patient))
                .thenReturn(Optional.empty());
        when(challengeRepository.countByPatientAndCreatedAtAfter(eq(patient), any()))
                .thenReturn(5L);

        ActivationRequestLimitedException exception = assertThrows(
            ActivationRequestLimitedException.class,
            () -> service.requestActivation(new PatientActivationRequest(patient.getDni()))
        );

        assertEquals(429, exception.getStatusCode());
        verify(emailSender, never()).send(
                anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void invalidOtpIncrementsFailedAttempts() {
        PatientActivationChallenge challenge = challenge(activePatient(41L), "otp-hash");
        challenge.setFailedAttempts(2);
        prepareLockedActivation(challenge);
        when(passwordEncoder.matches("000000", "otp-hash")).thenReturn(false);

        assertThrows(
            InvalidActivationCodeException.class,
            () -> service.verifyCode(verifyCodeRequest(challenge.getId(), "000000"))
        );

        assertEquals(3, challenge.getFailedAttempts());
        assertNull(challenge.getConsumedAt());
        verify(challengeRepository).save(challenge);
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void fifthInvalidOtpConsumesChallenge() {
        PatientActivationChallenge challenge = challenge(activePatient(41L), "otp-hash");
        challenge.setFailedAttempts(4);
        prepareLockedActivation(challenge);
        when(passwordEncoder.matches("000000", "otp-hash")).thenReturn(false);

        assertThrows(
            InvalidActivationCodeException.class,
            () -> service.verifyCode(verifyCodeRequest(challenge.getId(), "000000"))
        );

        assertEquals(5, challenge.getFailedAttempts());
        assertNotNull(challenge.getConsumedAt());
    }

    @Test
    void expiredOtpIsConsumedWithGenericError() {
        PatientActivationChallenge challenge = challenge(activePatient(41L), "otp-hash");
        challenge.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        prepareLockedActivation(challenge);

        InvalidActivationCodeException exception = assertThrows(
            InvalidActivationCodeException.class,
            () -> service.verifyCode(verifyCodeRequest(challenge.getId(), "123456"))
        );

        assertEquals("INVALID_ACTIVATION_CODE", exception.getCode());
        assertNotNull(challenge.getConsumedAt());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void validOtpOnlyEmitsHashedTemporaryTokenAndDoesNotCreateUser() {
        PatientActivationChallenge challenge = challenge(activePatient(41L), "otp-hash");
        prepareLockedActivation(challenge);
        when(passwordEncoder.matches("123456", "otp-hash")).thenReturn(true);
        when(passwordEncoder.encode(anyString()))
                .thenAnswer(invocation -> "TOKEN_HASH[" + invocation.getArgument(0) + "]");

        PatientActivationVerifyCodeResponse response = service.verifyCode(
            verifyCodeRequest(challenge.getId(), "123456")
        );

        assertEquals(challenge.getId(), response.challengeId());
        assertEquals(300L, response.expiresInSeconds());
        assertEquals(43, response.activationToken().length());
        assertEquals(
            "TOKEN_HASH[" + response.activationToken() + "]",
            challenge.getActivationTokenHash()
        );
        assertNotEquals(response.activationToken(), challenge.getActivationTokenHash());
        assertNotNull(challenge.getCodeVerifiedAt());
        assertNotNull(challenge.getActivationTokenExpiresAt());
        assertNull(challenge.getConsumedAt());
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void repeatedOtpVerificationDoesNotInvalidateAlreadyIssuedToken() {
        PatientActivationChallenge challenge = verifiedChallenge(activePatient(41L));
        prepareLockedActivation(challenge);

        assertThrows(
            InvalidActivationCodeException.class,
            () -> service.verifyCode(verifyCodeRequest(challenge.getId(), "123456"))
        );

        assertNull(challenge.getConsumedAt());
        assertEquals("activation-token-hash", challenge.getActivationTokenHash());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void validTemporaryTokenCreatesUserLinksPatientAndReturnsLogin() {
        Patient patient = activePatient(41L);
        PatientActivationChallenge challenge = verifiedChallenge(patient);
        PatientActivationCompleteRequest request = completeRequest(challenge.getId(), ACTIVATION_TOKEN);
        prepareLockedActivation(challenge);
        when(passwordEncoder.matches(ACTIVATION_TOKEN, "activation-token-hash"))
                .thenReturn(true);
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(90L);
            return user;
        });
        when(jwtUtil.generateToken(request.username(), Role.PATIENT.name()))
                .thenReturn("jwt-token");

        LoginResponse response = service.completeActivation(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertSame(savedUser, patient.getUser());
        assertEquals(Role.PATIENT, savedUser.getRole());
        assertEquals("encoded-password", savedUser.getPassword());
        assertNotNull(challenge.getConsumedAt());
        assertNull(challenge.getActivationTokenHash());
        assertEquals("jwt-token", response.token());
        assertEquals("PATIENT", response.role());
    }

    @Test
    void invalidTemporaryTokenCannotCreateCredentialsAndRemainsRetryable() {
        Patient patient = activePatient(41L);
        PatientActivationChallenge challenge = verifiedChallenge(patient);
        String invalidToken = "wrong-activation-token-with-at-least-32-characters";
        prepareLockedActivation(challenge);
        when(passwordEncoder.matches(invalidToken, "activation-token-hash"))
                .thenReturn(false);

        assertThrows(
            InvalidActivationCodeException.class,
            () -> service.completeActivation(completeRequest(challenge.getId(), invalidToken))
        );

        assertNull(challenge.getConsumedAt());
        assertEquals("activation-token-hash", challenge.getActivationTokenHash());
        assertNull(patient.getUser());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void expiredTemporaryTokenIsConsumedBeforeCredentialsAreCreated() {
        PatientActivationChallenge challenge = verifiedChallenge(activePatient(41L));
        challenge.setActivationTokenExpiresAt(LocalDateTime.now().minusSeconds(1));
        prepareLockedActivation(challenge);

        assertThrows(
            InvalidActivationCodeException.class,
            () -> service.completeActivation(
                completeRequest(challenge.getId(), ACTIVATION_TOKEN)
            )
        );

        assertNotNull(challenge.getConsumedAt());
        assertNull(challenge.getActivationTokenHash());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void duplicateUsernameLeavesTemporaryTokenUsableForAnotherUsername() {
        Patient patient = activePatient(41L);
        PatientActivationChallenge challenge = verifiedChallenge(patient);
        PatientActivationCompleteRequest request = completeRequest(challenge.getId(), ACTIVATION_TOKEN);
        prepareLockedActivation(challenge);
        when(passwordEncoder.matches(ACTIVATION_TOKEN, "activation-token-hash"))
                .thenReturn(true);
        when(userRepository.findByUsername(request.username()))
                .thenReturn(Optional.of(new User()));

        assertThrows(
            ResourceAlreadyExistsException.class,
            () -> service.completeActivation(request)
        );

        assertNull(challenge.getConsumedAt());
        assertEquals("activation-token-hash", challenge.getActivationTokenHash());
        assertNull(patient.getUser());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void staleTemporaryTokenCannotLinkASecondUser() {
        Patient patient = activePatient(41L);
        PatientActivationChallenge challenge = verifiedChallenge(patient);
        User alreadyLinked = new User();
        alreadyLinked.setUsername("already-linked");
        patient.setUser(alreadyLinked);
        prepareLockedActivation(challenge);

        assertThrows(
            InvalidActivationCodeException.class,
            () -> service.completeActivation(
                completeRequest(challenge.getId(), ACTIVATION_TOKEN)
            )
        );

        assertNotNull(challenge.getConsumedAt());
        assertNull(challenge.getActivationTokenHash());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    private void prepareAllowedRequest(Patient patient) {
        when(patientRepository.findByDniForUpdate(patient.getDni()))
                .thenReturn(Optional.of(patient));
        when(challengeRepository.findTopByPatientOrderByCreatedAtDesc(patient))
                .thenReturn(Optional.empty());
        when(challengeRepository.countByPatientAndCreatedAtAfter(eq(patient), any()))
                .thenReturn(0L);
        when(challengeRepository.findByPatientAndConsumedAtIsNull(patient))
                .thenReturn(List.of());
    }

    private void prepareLockedActivation(PatientActivationChallenge challenge) {
        Patient patient = challenge.getPatient();
        when(challengeRepository.findById(challenge.getId()))
                .thenReturn(Optional.of(challenge));
        when(patientRepository.findByIdForUpdate(patient.getId()))
                .thenReturn(Optional.of(patient));
        when(challengeRepository.findByIdForUpdate(challenge.getId()))
                .thenReturn(Optional.of(challenge));
    }

    private PatientActivationVerifyCodeRequest verifyCodeRequest(UUID challengeId, String code) {
        return new PatientActivationVerifyCodeRequest(challengeId, code);
    }

    private PatientActivationCompleteRequest completeRequest(UUID challengeId, String token) {
        return new PatientActivationCompleteRequest(
            challengeId,
            token,
            "maria.portal",
            "secret123"
        );
    }

    private PatientActivationChallenge verifiedChallenge(Patient patient) {
        PatientActivationChallenge challenge = challenge(patient, "otp-hash");
        challenge.setCodeVerifiedAt(LocalDateTime.now().minusSeconds(5));
        challenge.setActivationTokenHash("activation-token-hash");
        challenge.setActivationTokenExpiresAt(LocalDateTime.now().plusMinutes(4));
        return challenge;
    }

    private PatientActivationChallenge challenge(Patient patient, String otpHash) {
        PatientActivationChallenge challenge = new PatientActivationChallenge();
        challenge.setId(UUID.randomUUID());
        challenge.setPatient(patient);
        challenge.setOtpHash(otpHash);
        challenge.setFailedAttempts(0);
        challenge.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        return challenge;
    }

    private Patient activePatient(Long id) {
        LocalDateTime now = LocalDateTime.now();
        Patient patient = new Patient();
        patient.setId(id);
        patient.setDni("76543210");
        patient.setFirstName("Maria");
        patient.setLastName("Torres");
        patient.setGender(Gender.FEMALE);
        patient.setDateOfBirth(LocalDate.of(1995, 4, 10));
        patient.setPhone("999888777");
        patient.setEmail("maria@example.com");
        patient.setAddress("Lima");
        patient.setStatus(UserStatus.ACTIVE);
        patient.setCreatedAt(now);
        patient.setUpdatedAt(now);
        return patient;
    }

    private String extractOtp(String message) {
        Matcher matcher = OTP_PATTERN.matcher(message);
        assertFalse(message.isBlank());
        if (!matcher.find()) {
            throw new AssertionError("El correo no contiene un OTP de seis digitos");
        }
        return matcher.group(1);
    }
}
