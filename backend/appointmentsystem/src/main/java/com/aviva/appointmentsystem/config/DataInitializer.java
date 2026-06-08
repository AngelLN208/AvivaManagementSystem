package com.aviva.appointmentsystem.config;

import com.aviva.appointmentsystem.entity.Role;
import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Inicializador de datos de prueba (Seed Data).
 *
 * Se ejecuta automáticamente al arrancar la aplicación (ApplicationRunner).
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
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("=== DataInitializer: Iniciando seed de usuarios ===");

        createUserIfNotExists("admin",      "admin123",    "Admin",      "Sistema",    Role.ADMIN);
        createUserIfNotExists("doctor1",    "doctor123",   "Carlos",     "Mendoza",    Role.DOCTOR);
        createUserIfNotExists("recepcion1", "recep123",    "María",      "López",      Role.RECEPTIONIST);
        createUserIfNotExists("paciente1",  "paciente123", "Juan",       "García",     Role.PATIENT);

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
    private void createUserIfNotExists(String username, String rawPassword,
                                        String firstName, String lastName, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            logger.debug("Usuario '{}' ya existe — omitido", username);
            return;
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

        userRepository.save(user);
        logger.info("Usuario seed creado: {} ({})", username, role.name());
    }
}
