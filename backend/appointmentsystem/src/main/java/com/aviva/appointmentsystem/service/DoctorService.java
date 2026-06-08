package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.DoctorRequest;
import com.aviva.appointmentsystem.dto.DoctorResponse;
import com.aviva.appointmentsystem.dto.DoctorUpdateRequest;
import com.aviva.appointmentsystem.dto.SpecialtyResponse;
import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.Specialty;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.ResourceAlreadyExistsException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.DoctorRepository;
import com.aviva.appointmentsystem.repository.SpecialtyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar médicos.
 * 
 * Responsabilidades (Service Layer):
 * - TODA la lógica de negocio reside aquí
 * - RN-36: Validar que specialtyId exista antes de crear/actualizar
 * - Validaciones de unicidad (licenseNumber, email)
 * - Búsqueda por ID con excepción si no existe
 * - Conversión Entity → DTO (mapToResponse)
 * 
 * Seguridad:
 * - RN-41: Solo ADMIN puede crear/actualizar/desactivar médicos
 *   → Se aplica con @PreAuthorize en el Controller
 * 
 * El Controller NO hace lógica, solo delega aquí.
 */
@Service
@Transactional
public class DoctorService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         SpecialtyRepository specialtyRepository) {
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
    }

    /**
     * Crea un nuevo médico.
     * 
     * RN-36: Valida que specialtyId exista.
     * Valida unicidad de licenseNumber y email.
     */
    public DoctorResponse create(DoctorRequest request) {
        logger.info("Creando nuevo doctor: {} {}", request.firstName(), request.lastName());

        // Validar unicidad de licencia
        if (doctorRepository.findByLicenseNumber(request.licenseNumber()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                "Ya existe un doctor con número de licencia: " + request.licenseNumber()
            );
        }

        // Validar unicidad de email
        if (!doctorRepository.findByEmail(request.email()).isEmpty()) {
            throw new ResourceAlreadyExistsException(
                "Ya existe un doctor con email: " + request.email()
            );
        }

        // RN-36: Validar que la especialidad exista
        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", request.specialtyId()));

        LocalDateTime now = LocalDateTime.now();

        Doctor doctor = new Doctor();
        doctor.setFirstName(request.firstName());
        doctor.setLastName(request.lastName());
        doctor.setLicenseNumber(request.licenseNumber());
        doctor.setPhone(request.phone());
        doctor.setEmail(request.email());
        doctor.setSpecialty(specialty);
        doctor.setStatus(UserStatus.ACTIVE);
        doctor.setCreatedAt(now);
        doctor.setUpdatedAt(now);

        Doctor saved = doctorRepository.save(doctor);
        logger.info("Doctor creado: ID={}, licencia={}", saved.getId(), saved.getLicenseNumber());

        return mapToResponse(saved);
    }

    /**
     * Actualiza un médico existente (actualización parcial).
     * 
     * Solo actualiza los campos que vienen como no-nulos en el request.
     * El Postman puede enviar solo { "phone": "999333555" }.
     * 
     * @param id ID del doctor a actualizar
     * @param request datos parciales a actualizar
     * @return doctor actualizado
     * @throws ResourceNotFoundException si el ID no existe
     * @throws ResourceAlreadyExistsException si la nueva licencia/email ya está en uso
     */
    public DoctorResponse update(Long id, DoctorUpdateRequest request) {
        logger.info("Actualizando doctor ID={}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        // Actualizar solo los campos presentes (no nulos)

        if (request.firstName() != null) {
            doctor.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            doctor.setLastName(request.lastName());
        }

        if (request.licenseNumber() != null && !doctor.getLicenseNumber().equals(request.licenseNumber())) {
            doctorRepository.findByLicenseNumber(request.licenseNumber()).ifPresent(existing -> {
                throw new ResourceAlreadyExistsException(
                    "Ya existe un doctor con número de licencia: " + request.licenseNumber()
                );
            });
            doctor.setLicenseNumber(request.licenseNumber());
        }

        if (request.phone() != null) {
            doctor.setPhone(request.phone());
        }

        if (request.email() != null && !doctor.getEmail().equals(request.email())) {
            if (!doctorRepository.findByEmail(request.email()).isEmpty()) {
                throw new ResourceAlreadyExistsException(
                    "Ya existe un doctor con email: " + request.email()
                );
            }
            doctor.setEmail(request.email());
        }

        // RN-36: Si se cambia la especialidad, validar que exista
        if (request.specialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.specialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Especialidad", request.specialtyId()));
            doctor.setSpecialty(specialty);
        }

        doctor.setUpdatedAt(LocalDateTime.now());

        Doctor updated = doctorRepository.save(doctor);
        logger.info("Doctor actualizado: ID={}", id);

        return mapToResponse(updated);
    }

    /**
     * Obtiene un médico por ID.
     * 
     * @param id ID del doctor
     * @return doctor encontrado con su especialidad anidada
     * @throws ResourceNotFoundException si el ID no existe
     */
    @Transactional(readOnly = true)
    public DoctorResponse getById(Long id) {
        logger.debug("Obteniendo doctor ID={}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        return mapToResponse(doctor);
    }

    /**
     * Lista todos los médicos activos.
     * Usa findByStatus() para filtrar directamente en la DB.
     */
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAll() {
        logger.debug("Obteniendo todos los doctores activos");

        return doctorRepository.findByStatus(UserStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene médicos activos de una especialidad.
     * 
     * RN-36: Valida que la especialidad exista antes de buscar.
     * Usa findBySpecialtyAndStatus() para filtrar en la DB.
     * 
     * @param specialtyId ID de la especialidad
     * @return lista de doctores activos de esa especialidad
     * @throws ResourceNotFoundException si la especialidad no existe
     */
    @Transactional(readOnly = true)
    public List<DoctorResponse> getBySpecialty(Long specialtyId) {
        logger.debug("Obteniendo doctores por especialidad ID={}", specialtyId);

        // Validar que la especialidad exista
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", specialtyId));

        return doctorRepository.findBySpecialtyAndStatus(specialty, UserStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Desactiva un médico (soft delete).
     * 
     * @param id ID del doctor a desactivar
     * @throws ResourceNotFoundException si el ID no existe
     */
    public void deactivate(Long id) {
        logger.info("Desactivando doctor ID={}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        doctor.setStatus(UserStatus.INACTIVE);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        logger.info("Doctor desactivado: ID={}, licencia={}", id, doctor.getLicenseNumber());
    }

    // ===============================
    // MÉTODOS PRIVADOS DE CONVERSIÓN
    // ===============================

    /**
     * Mapea entidad Doctor a DTO DoctorResponse.
     * Incluye SpecialtyResponse anidado.
     */
    private DoctorResponse mapToResponse(Doctor doctor) {
        SpecialtyResponse specialtyResponse = new SpecialtyResponse(
            doctor.getSpecialty().getId(),
            doctor.getSpecialty().getName(),
            doctor.getSpecialty().getDescription(),
            doctor.getSpecialty().getActive(),
            doctor.getSpecialty().getCreatedAt(),
            doctor.getSpecialty().getUpdatedAt()
        );

        return new DoctorResponse(
            doctor.getId(),
            doctor.getFirstName(),
            doctor.getLastName(),
            doctor.getLicenseNumber(),
            doctor.getPhone(),
            doctor.getEmail(),
            specialtyResponse,
            doctor.getStatus().name(),
            doctor.getCreatedAt(),
            doctor.getUpdatedAt()
        );
    }
}
