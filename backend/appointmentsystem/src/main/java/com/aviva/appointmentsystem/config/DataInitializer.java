package com.aviva.appointmentsystem.config;

import com.aviva.appointmentsystem.entity.Gender;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.Role;
import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.repository.PatientRepository;
import com.aviva.appointmentsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inicializador de datos de prueba (Seed Data).
 *
 * Se ejecuta al arrancar únicamente cuando app.seed.enabled=true.
 * La propiedad está deshabilitada por defecto para no crear credenciales
 * conocidas fuera de entornos controlados de desarrollo o pruebas.
 * Usa findByUsername() para verificar existencia antes de insertar,
 * por lo que es IDEMPOTENTE: si los usuarios ya existen no los vuelve a crear.
 *
 * Compatible tanto con ddl-auto=create como con ddl-auto=update.
 *
 * Usuarios creados:
 * ┌──────────────┬────────────┬───────────────┐
 * │ username     │ password   │ role          │
 * ├──────────────┼────────────┼───────────────┤
 * │ admin        │ admin123   │ ADMIN         │
 * │ doctor1      │ doctor123  │ DOCTOR        │
 * │ recepcion1   │ recep123   │ RECEPTIONIST  │
 * │ paciente1    │ paciente123│ PATIENT       │
 * └──────────────┴────────────┴───────────────┘
 *
 * Las contraseñas están hasheadas con BCrypt al guardarlas.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final String SEED_PATIENT_DNI = "00000001";
    private static final String SEED_PATIENT_EMAIL = "paciente1@aviva.test";

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PatientRepository patientRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        logger.info("=== DataInitializer: Iniciando seed de usuarios ===");

        createUserIfNotExists("admin",      "admin123",    "Admin",      "Sistema",    Role.ADMIN);
        createUserIfNotExists("doctor1",    "doctor123",   "Carlos",     "Mendoza",    Role.DOCTOR);
        createUserIfNotExists("recepcion1", "recep123",    "María",      "López",      Role.RECEPTIONIST);
        User portalPatientUser = createUserIfNotExists(
                "paciente1", "paciente123", "Juan", "García", Role.PATIENT);
        ensureSeedPatientProfile(portalPatientUser);

        logger.info("=== DataInitializer: Seed completado ===");
        logger.info("=== Credenciales de prueba:");
        logger.info("=== ADMIN        → username: admin,      password: admin123");
        logger.info("=== DOCTOR       → username: doctor1,    password: doctor123");
        logger.info("=== RECEPTIONIST → username: recepcion1, password: recep123");
        logger.info("=== PATIENT      → username: paciente1,  password: paciente123");
    }

    /**
     * Crea un usuario solo si no existe aún (idempotente).
     * Evita duplicados si la app se reinicia con ddl-auto=update.
     */
    private User createUserIfNotExists(String username, String rawPassword,
                                       String firstName, String lastName, Role role) {
        User existing = userRepository.findByUsername(username).orElse(null);
        if (existing != null) {
            logger.debug("Usuario '{}' ya existe — omitido", username);
            return existing;
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User saved = userRepository.save(user);
        logger.info("Usuario seed creado: {} ({})", username, role.name());
        return saved;
    }

    /**
     * Garantiza que las credenciales seed de paciente también funcionen con
     * los endpoints /me. No intenta enlazar pacientes reales por nombre.
     */
    private void ensureSeedPatientProfile(User user) {
        if (user.getRole() != Role.PATIENT) {
            logger.warn("No se creó el perfil seed: paciente1 existe con otro rol");
            return;
        }

        if (patientRepository.findByUserUsername(user.getUsername()).isPresent()) {
            return;
        }

        Patient existingByDni = patientRepository.findByDni(SEED_PATIENT_DNI).orElse(null);
        if (existingByDni != null) {
            // Nunca reclamar una fila existente usando solo datos personales.
            logger.warn("No se creó el perfil de paciente1: el DNI seed ya está ocupado");
            return;
        }

        if (patientRepository.findByEmail(SEED_PATIENT_EMAIL).isPresent()) {
            logger.warn("No se creó el perfil de paciente1: el email seed ya está ocupado");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDni(SEED_PATIENT_DNI);
        patient.setFirstName("Juan");
        patient.setLastName("García");
        patient.setGender(Gender.MALE);
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setPhone("999999999");
        patient.setEmail(SEED_PATIENT_EMAIL);
        patient.setAddress("Perfil de demostración");
        patient.setStatus(UserStatus.ACTIVE);
        patient.setCreatedAt(now);
        patient.setUpdatedAt(now);

        patientRepository.save(patient);
        logger.info("Perfil seed creado y vinculado al usuario paciente1");
    }
}
