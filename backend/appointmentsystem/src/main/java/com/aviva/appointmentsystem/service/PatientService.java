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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Servicio para gestionar pacientes
 */
@Service
@Transactional
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private PatientRepository patientRepository;

    /**
     * Crea un nuevo paciente
     */
    public PatientResponse create(PatientRequest request) {
        logger.info("Creando nuevo paciente con DNI: {}", request.dni());

        // Validar que no exista paciente con el mismo DNI
        if (patientRepository.findByDni(request.dni()).isPresent()) {
            logger.warn("Ya existe un paciente con DNI: {}", request.dni());
            throw new ResourceAlreadyExistsException("Ya existe un paciente con DNI: " + request.dni());
        }

        // Validar que no exista paciente con el mismo email
        if (patientRepository.findByEmail(request.email()).isPresent()) {
            logger.warn("Ya existe un paciente con email: {}", request.email());
            throw new ResourceAlreadyExistsException("Ya existe un paciente con email: " + request.email());
        }

        Patient patient = new Patient();
        patient.setDni(request.dni());
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setGender(Gender.valueOf(request.gender().toUpperCase()));
        patient.setDateOfBirth(parseDate(request.dateOfBirth()));
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setStatus(UserStatus.ACTIVE);
        patient.setCreatedAt(LocalDateTime.now());
        patient.setUpdatedAt(LocalDateTime.now());

        Patient saved = patientRepository.save(patient);
        logger.info("Paciente creado exitosamente: ID={}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Actualiza un paciente
     */
    public PatientResponse update(Long id, PatientRequest request) {
        logger.info("Actualizando paciente ID={}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));

        // Validar DNI si cambió
        if (!patient.getDni().equals(request.dni()) &&
            patientRepository.findByDni(request.dni()).isPresent()) {
            throw new ResourceAlreadyExistsException("Ya existe un paciente con DNI: " + request.dni());
        }

        // Validar email si cambió
        if (!patient.getEmail().equals(request.email()) &&
            patientRepository.findByEmail(request.email()).isPresent()) {
            throw new ResourceAlreadyExistsException("Ya existe un paciente con email: " + request.email());
        }

        patient.setDni(request.dni());
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setGender(Gender.valueOf(request.gender().toUpperCase()));
        patient.setDateOfBirth(parseDate(request.dateOfBirth()));
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setUpdatedAt(LocalDateTime.now());

        Patient updated = patientRepository.save(patient);
        logger.info("Paciente actualizado: ID={}", id);

        return mapToResponse(updated);
    }

    /**
     * Obtiene un paciente por ID
     */
    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        logger.debug("Obteniendo paciente ID={}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));

        return mapToResponse(patient);
    }

    /**
     * Obtiene un paciente por DNI
     */
    @Transactional(readOnly = true)
    public PatientResponse getByDni(String dni) {
        logger.debug("Obteniendo paciente por DNI: {}", dni);

        Patient patient = patientRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente con DNI: " + dni));

        return mapToResponse(patient);
    }

    /**
     * Lista todos los pacientes activos
     */
    @Transactional(readOnly = true)
    public List<PatientResponse> getAll() {
        logger.debug("Obteniendo todos los pacientes");
        return patientRepository.findAll()
                .stream()
                .filter(p -> p.getStatus() == UserStatus.ACTIVE)
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Busca pacientes por nombre
     */
    @Transactional(readOnly = true)
    public List<PatientResponse> searchByName(String firstName, String lastName) {
        logger.debug("Buscando pacientes por nombre: {} {}", firstName, lastName);

        List<Patient> patients;

        if (firstName != null && lastName != null) {
            patients = patientRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName);
        } else if (firstName != null) {
            patients = patientRepository.findByFirstNameContainingIgnoreCase(firstName);
        } else if (lastName != null) {
            patients = patientRepository.findByLastNameContainingIgnoreCase(lastName);
        } else {
            return getAll();
        }

        return patients.stream()
                .filter(p -> p.getStatus() == UserStatus.ACTIVE)
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Desactiva un paciente
     */
    public void deactivate(Long id) {
        logger.info("Desactivando paciente ID={}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));

        patient.setStatus(UserStatus.INACTIVE);
        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);

        logger.info("Paciente desactivado: ID={}", id);
    }

    /**
     * Mapea entidad a DTO
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
     * Parsea una fecha en formato String a LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            logger.error("Error parseando fecha: {}", dateStr);
            throw new ValidationException("Formato de fecha inválido. Use: yyyy-MM-dd");
        }
    }
}
