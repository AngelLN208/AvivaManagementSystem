package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.LoginResponse;
import com.aviva.appointmentsystem.dto.PatientResponse;
import com.aviva.appointmentsystem.dto.RegisterPatientRequest;
import com.aviva.appointmentsystem.entity.Gender;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.Role;
import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.InvalidCredentialsException;
import com.aviva.appointmentsystem.exception.ResourceAlreadyExistsException;
import com.aviva.appointmentsystem.exception.UserInactiveException;
import com.aviva.appointmentsystem.exception.UserNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.PatientRepository;
import com.aviva.appointmentsystem.repository.UserRepository;
import com.aviva.appointmentsystem.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Servicio de autenticación.
 * 
 * Responsabilidades:
 * - Autenticar usuarios (login) → devuelve LoginResponse con token JWT
 * - Registrar pacientes (auto-registro) → crea User + Patient en una sola transacción
 * 
 * Flujo login: Controller → AuthService.login() → UserRepository → JwtUtil
 * Flujo registro: Controller → AuthService.registerPatient() → UserRepository + PatientRepository
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PatientRepository patientRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Autentica un usuario y devuelve un LoginResponse con token JWT y datos del usuario.
     *
     * @param username el nombre de usuario
     * @param password la contraseña en texto plano
     * @return LoginResponse con token, rol, username, firstName, lastName
     * @throws UserNotFoundException si el usuario no existe
     * @throws InvalidCredentialsException si la contraseña es incorrecta
     * @throws UserInactiveException si el usuario está inactivo
     */
    @Transactional(readOnly = true)
    public LoginResponse login(String username, String password) {
        logger.info("Intento de login para usuario: {}", username);

        // 1. Buscar usuario por nombre de usuario
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado: {}", username);
                    return new UserNotFoundException(username);
                });

        // 2. Validar contraseña
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Contraseña incorrecta para usuario: {}", username);
            throw new InvalidCredentialsException();
        }

        // 3. Validar que el usuario esté activo
        if (user.getStatus() != UserStatus.ACTIVE) {
            logger.warn("Usuario inactivo intenta acceder: {}", username);
            throw new UserInactiveException(username);
        }

        // 4. Generar token JWT
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        logger.info("Login exitoso para usuario: {} con rol: {}", username, user.getRole());

        // 5. Devolver LoginResponse (DTO, no Map)
        return new LoginResponse(
            token,
            user.getRole().name(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName()
        );
    }

    /**
     * Registra un nuevo paciente creando tanto el User como el Patient.
     * 
     * Validaciones del SERVICE (no del Controller):
     * - Verifica que el username no exista
     * - Verifica que el DNI no exista
     * - Verifica que el email no exista
     * - Verifica que el género sea válido
     * - Verifica que la fecha de nacimiento sea válida
     *
     * @param request DTO con datos de autenticación y datos personales
     * @return PatientResponse con los datos del paciente creado
     */
    public PatientResponse registerPatient(RegisterPatientRequest request) {
        logger.info("Registro de paciente: username={}, dni={}", request.username(), request.dni());

        // ── Validaciones de unicidad (responsabilidad del Service) ──

        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                "Ya existe un usuario con el nombre de usuario: " + request.username()
            );
        }

        if (patientRepository.findByDni(request.dni()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                "Ya existe un paciente con DNI: " + request.dni()
            );
        }

        if (patientRepository.findByEmail(request.email()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                "Ya existe un paciente con email: " + request.email()
            );
        }

        // ── Validar género ──
        Gender gender;
        try {
            gender = Gender.valueOf(request.gender().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                "Género inválido: '" + request.gender() + "'. Valores permitidos: MALE, FEMALE, OTHER"
            );
        }

        // ── Validar fecha de nacimiento ──
        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(request.dateOfBirth(), DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Formato de fecha inválido. Use: yyyy-MM-dd");
        }

        LocalDateTime now = LocalDateTime.now();

        // ── 1. Crear el User (credenciales de acceso) ──
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(Role.PATIENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userRepository.save(user);
        logger.info("User creado: username={}, role=PATIENT", user.getUsername());

        // ── 2. Crear el Patient (datos clínicos/personales) ──
        Patient patient = new Patient();
        // Vincula de forma explícita la identidad del portal con su perfil.
        patient.setUser(user);
        patient.setDni(request.dni());
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setGender(gender);
        patient.setDateOfBirth(dateOfBirth);
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setStatus(UserStatus.ACTIVE);
        patient.setCreatedAt(now);
        patient.setUpdatedAt(now);

        Patient saved = patientRepository.save(patient);
        logger.info("Patient creado: id={}, dni={}", saved.getId(), saved.getDni());

        // ── 3. Devolver respuesta ──
        return new PatientResponse(
            saved.getId(),
            saved.getDni(),
            saved.getFirstName(),
            saved.getLastName(),
            saved.getGender().name(),
            saved.getDateOfBirth(),
            saved.getPhone(),
            saved.getEmail(),
            saved.getAddress(),
            saved.getStatus().name(),
            saved.getCreatedAt(),
            saved.getUpdatedAt()
        );
    }
}
