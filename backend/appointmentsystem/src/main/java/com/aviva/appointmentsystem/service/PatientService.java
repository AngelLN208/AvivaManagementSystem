package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.PatientRequest;
import com.aviva.appointmentsystem.dto.PatientResponse;
import com.aviva.appointmentsystem.entity.Gender;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.ResourceAlreadyExistsException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar pacientes.
 * 
 * Responsabilidades (Service Layer):
 * - TODA la lógica de negocio reside aquí
 * - Validaciones de unicidad (DNI, email)
 * - Validación de género (enum Gender)
 * - Resolución de relaciones (el Controller NUNCA lo hace)
 * - Búsqueda por ID/DNI con excepción si no existe
 * - Búsqueda flexible con filtros combinados
 * - Conversión Entity → DTO (mapToResponse)
 * 
 * El Controller NO hace lógica, solo delega aquí.
 */
@Service
@Transactional
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Crea un nuevo paciente (uso interno del personal).
     * 
     * Validaciones del Service:
     * - DNI no duplicado
     * - Email no duplicado
     * - Género válido (MALE, FEMALE, OTHER)
     * 
     * NOTA: Para auto-registro de pacientes (que crea User + Patient),
     * se usa AuthService.registerPatient().
     */
    public PatientResponse create(PatientRequest request) {
        logger.info("Creando nuevo paciente con DNI: {}", request.dni());

        // Validar que no exista paciente con el mismo DNI
        if (patientRepository.findByDni(request.dni()).isPresent()) {
            logger.warn("Ya existe un paciente con DNI: {}", request.dni());
            throw new ResourceAlreadyExistsException(
                "Ya existe un paciente con DNI: " + request.dni()
            );
        }

        // Validar que no exista paciente con el mismo email
        if (patientRepository.findByEmail(request.email()).isPresent()) {
            logger.warn("Ya existe un paciente con email: {}", request.email());
            throw new ResourceAlreadyExistsException(
                "Ya existe un paciente con email: " + request.email()
            );
        }

        // Validar y convertir género
        Gender gender = parseGender(request.gender());

        LocalDateTime now = LocalDateTime.now();

        Patient patient = new Patient();
        patient.setDni(request.dni());
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setGender(gender);
        patient.setDateOfBirth(request.dateOfBirth()); // Ya es LocalDate con @Past validado
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setStatus(UserStatus.ACTIVE);
        patient.setCreatedAt(now);
        patient.setUpdatedAt(now);

        Patient saved = patientRepository.save(patient);
        logger.info("Paciente creado exitosamente: ID={}, DNI={}", saved.getId(), saved.getDni());

        return mapToResponse(saved);
    }

    /**
     * Actualiza un paciente existente.
     * 
     * @param id ID del paciente a actualizar
     * @param request datos actualizados
     * @return paciente actualizado
     * @throws ResourceNotFoundException si el ID no existe
     * @throws ResourceAlreadyExistsException si el nuevo DNI/email ya está en uso por OTRO paciente
     */
    public PatientResponse update(Long id, PatientRequest request) {
        logger.info("Actualizando paciente ID={}", id);

        // 1. Buscar el paciente o lanzar 404
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));

        // 2. Si cambió el DNI, verificar que no exista otro con el mismo DNI
        if (!patient.getDni().equals(request.dni())) {
            patientRepository.findByDni(request.dni()).ifPresent(existing -> {
                throw new ResourceAlreadyExistsException(
                    "Ya existe un paciente con DNI: " + request.dni()
                );
            });
        }

        // 3. Si cambió el email, verificar que no exista otro con el mismo email
        if (!patient.getEmail().equals(request.email())) {
            patientRepository.findByEmail(request.email()).ifPresent(existing -> {
                throw new ResourceAlreadyExistsException(
                    "Ya existe un paciente con email: " + request.email()
                );
            });
        }

        // 4. Validar y convertir género
        Gender gender = parseGender(request.gender());

        // 5. Actualizar campos
        patient.setDni(request.dni());
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setGender(gender);
        patient.setDateOfBirth(request.dateOfBirth()); // Ya es LocalDate
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setUpdatedAt(LocalDateTime.now());

        Patient updated = patientRepository.save(patient);
        logger.info("Paciente actualizado: ID={}", id);

        return mapToResponse(updated);
    }

    /**
     * Obtiene un paciente por ID.
     * 
     * @param id ID del paciente
     * @return paciente encontrado
     * @throws ResourceNotFoundException si el ID no existe
     */
    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        logger.debug("Obteniendo paciente ID={}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));

        return mapToResponse(patient);
    }

    /**
     * Obtiene un paciente por DNI.
     * 
     * @param dni DNI del paciente (8 dígitos)
     * @return paciente encontrado
     * @throws ResourceNotFoundException si no existe paciente con ese DNI
     */
    @Transactional(readOnly = true)
    public PatientResponse getByDni(String dni) {
        logger.debug("Obteniendo paciente por DNI: {}", dni);

        Patient patient = patientRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Paciente con DNI: " + dni + " no encontrado"
                ));

        return mapToResponse(patient);
    }

    /**
     * Lista todos los pacientes activos.
     * 
     * Usa findByStatus() para filtrar directamente en la DB
     * (evita cargar todos y filtrar en Java — antipatrón).
     */
    @Transactional(readOnly = true)
    public List<PatientResponse> getAll() {
        logger.debug("Obteniendo todos los pacientes activos");

        return patientRepository.findByStatus(UserStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Busca pacientes activos por nombre y/o apellido.
     * Usa búsqueda LIKE case-insensitive.
     * 
     * @param firstName nombre parcial (puede ser null)
     * @param lastName apellido parcial (puede ser null)
     * @return lista de pacientes que coincidan con los criterios
     */
    @Transactional(readOnly = true)
    public List<PatientResponse> searchByName(String firstName, String lastName) {
        logger.debug("Buscando pacientes por nombre: {} {}", firstName, lastName);

        // Usar la query flexible que soporta parámetros nulos
        return patientRepository.searchByFilters(
                UserStatus.ACTIVE,
                null,       // sin filtro por DNI
                firstName,
                lastName
            )
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    /**
     * Desactiva un paciente (soft delete).
     * No lo elimina físicamente de la base de datos.
     * 
     * @param id ID del paciente a desactivar
     * @throws ResourceNotFoundException si el ID no existe
     */
    public void deactivate(Long id) {
        logger.info("Desactivando paciente ID={}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));

        patient.setStatus(UserStatus.INACTIVE);
        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);

        logger.info("Paciente desactivado: ID={}, DNI={}", id, patient.getDni());
    }

    // ===============================
    // MÉTODOS PRIVADOS DE CONVERSIÓN
    // ===============================

    /**
     * Mapea entidad Patient a DTO PatientResponse.
     * Método privado — la conversión es responsabilidad del Service.
     */
    private PatientResponse mapToResponse(Patient patient) {
        return new PatientResponse(
            patient.getId(),
            patient.getDni(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getGender().name(),
            patient.getDateOfBirth(),
            patient.getPhone(),
            patient.getEmail(),
            patient.getAddress(),
            patient.getStatus().name(),
            patient.getCreatedAt(),
            patient.getUpdatedAt()
        );
    }

    /**
     * Parsea y valida el string de género contra el enum Gender.
     * 
     * @param genderStr string del género (ej: "MALE", "male")
     * @return Gender enum
     * @throws ValidationException si el valor no es válido
     */
    private Gender parseGender(String genderStr) {
        try {
            return Gender.valueOf(genderStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                "Género inválido: '" + genderStr + "'. Valores permitidos: MALE, FEMALE, OTHER"
            );
        }
    }
}
