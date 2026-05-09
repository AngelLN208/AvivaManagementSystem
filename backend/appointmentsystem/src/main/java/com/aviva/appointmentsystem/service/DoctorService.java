package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.DoctorRequest;
import com.aviva.appointmentsystem.dto.DoctorResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar doctores
 */
@Service
@Transactional
public class DoctorService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    /**
     * Crea un nuevo doctor
     */
    public DoctorResponse create(DoctorRequest request) {
        logger.info("Creando nuevo doctor: {} {}", request.firstName(), request.lastName());

        // Validar que no exista doctor con el mismo número de licencia
        if (doctorRepository.findByLicenseNumber(request.licenseNumber()).isPresent()) {
            logger.warn("Ya existe un doctor con número de licencia: {}", request.licenseNumber());
            throw new ResourceAlreadyExistsException("Ya existe un doctor con número de licencia: " + request.licenseNumber());
        }

        // Validar que la especialidad exista
        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", request.specialtyId()));

        Doctor doctor = new Doctor();
        doctor.setFirstName(request.firstName());
        doctor.setLastName(request.lastName());
        doctor.setLicenseNumber(request.licenseNumber());
        doctor.setPhone(request.phone());
        doctor.setEmail(request.email());
        doctor.setSpecialty(specialty);
        doctor.setStatus(UserStatus.ACTIVE);
        doctor.setCreatedAt(LocalDateTime.now());
        doctor.setUpdatedAt(LocalDateTime.now());

        Doctor saved = doctorRepository.save(doctor);
        logger.info("Doctor creado exitosamente: ID={}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Actualiza un doctor
     */
    public DoctorResponse update(Long id, DoctorRequest request) {
        logger.info("Actualizando doctor ID={}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        // Validar licencia si cambió
        if (!doctor.getLicenseNumber().equals(request.licenseNumber()) &&
            doctorRepository.findByLicenseNumber(request.licenseNumber()).isPresent()) {
            throw new ResourceAlreadyExistsException("Ya existe un doctor con número de licencia: " + request.licenseNumber());
        }

        // Validar especialidad
        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", request.specialtyId()));

        doctor.setFirstName(request.firstName());
        doctor.setLastName(request.lastName());
        doctor.setLicenseNumber(request.licenseNumber());
        doctor.setPhone(request.phone());
        doctor.setEmail(request.email());
        doctor.setSpecialty(specialty);
        doctor.setUpdatedAt(LocalDateTime.now());

        Doctor updated = doctorRepository.save(doctor);
        logger.info("Doctor actualizado: ID={}", id);

        return mapToResponse(updated);
    }

    /**
     * Obtiene un doctor por ID
     */
    @Transactional(readOnly = true)
    public DoctorResponse getById(Long id) {
        logger.debug("Obteniendo doctor ID={}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        return mapToResponse(doctor);
    }

    /**
     * Lista todos los doctores activos
     */
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAll() {
        logger.debug("Obteniendo todos los doctores");
        return doctorRepository.findAll()
                .stream()
                .filter(d -> d.getStatus() == UserStatus.ACTIVE)
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene doctores por especialidad
     */
    @Transactional(readOnly = true)
    public List<DoctorResponse> getBySpecialty(Long specialtyId) {
        logger.debug("Obteniendo doctores por especialidad ID={}", specialtyId);

        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad", specialtyId));

        return doctorRepository.findBySpecialty(specialty)
                .stream()
                .filter(d -> d.getStatus() == UserStatus.ACTIVE)
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Desactiva un doctor
     */
    public void deactivate(Long id) {
        logger.info("Desactivando doctor ID={}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        doctor.setStatus(UserStatus.INACTIVE);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        logger.info("Doctor desactivado: ID={}", id);
    }

    /**
     * Mapea entidad a DTO
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
