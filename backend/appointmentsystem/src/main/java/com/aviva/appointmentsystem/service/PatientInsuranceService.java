package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.PatientInsuranceRequest;
import com.aviva.appointmentsystem.dto.PatientInsuranceResponse;
import com.aviva.appointmentsystem.dto.PortalPatientInsuranceRequest;
import com.aviva.appointmentsystem.entity.Insurance;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.PatientInsurance;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.BusinessRuleException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.UserInactiveException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.InsuranceRepository;
import com.aviva.appointmentsystem.repository.PatientInsuranceRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar seguros de pacientes
 * RN-25: Validación de seguros primarios y activos
 */
@Service
@Transactional
public class PatientInsuranceService {

    private static final Logger logger = LoggerFactory.getLogger(PatientInsuranceService.class);

    @Autowired
    private PatientInsuranceRepository patientInsuranceRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private InsuranceRepository insuranceRepository;

    /**
     * Vincula un seguro a un paciente
     */
    public PatientInsuranceResponse linkInsurance(Long patientId, PatientInsuranceRequest request) {
        logger.info("Vinculando seguro a paciente: patientId={}, insuranceId={}", patientId, request.insuranceId());

        // Validar que existan paciente y seguro
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", patientId));

        Insurance insurance = insuranceRepository.findById(request.insuranceId())
                .orElseThrow(() -> new ResourceNotFoundException("Seguro", request.insuranceId()));

        // RN-07: Validar que el seguro esté activo
        if (!insurance.getActive()) {
            throw new ValidationException("El seguro no está activo");
        }

        // RN-25: Si este es el primario, desactivar el anterior
        if (request.isPrimary() != null && request.isPrimary()) {
            patientInsuranceRepository.findByPatientAndIsPrimary(patient, true).ifPresent(existing -> {
                logger.info("Desactivando seguro primario anterior para paciente: {}", patientId);
                existing.setIsPrimary(false);
                patientInsuranceRepository.save(existing);
            });
        }

        // Validar fechas
        if (request.expirationDate().isBefore(request.effectiveDate())) {
            throw new ValidationException("La fecha de fin no puede ser anterior a la de inicio");
        }

        // Crear vinculación
        PatientInsurance patientInsurance = new PatientInsurance();
        patientInsurance.setPatient(patient);
        patientInsurance.setInsurance(insurance);
        patientInsurance.setPolicyNumber(request.policyNumber());
        patientInsurance.setUsedAnnualCoverage(BigDecimal.ZERO);
        patientInsurance.setPolicyHolderName(request.policyHolderName());
        patientInsurance.setRelationshipToHolder(request.relationshipToHolder());
        patientInsurance.setIsPrimary(request.isPrimary() != null && request.isPrimary());
        patientInsurance.setEffectiveDate(request.effectiveDate());
        patientInsurance.setExpirationDate(request.expirationDate());
        patientInsurance.setActive(true);
        patientInsurance.setCreatedAt(LocalDateTime.now());
        patientInsurance.setUpdatedAt(LocalDateTime.now());

        PatientInsurance saved = patientInsuranceRepository.save(patientInsurance);
        logger.info("Seguro vinculado al paciente: ID={}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Vincula la única póliza activa permitida en el portal al paciente
     * autenticado. El patientId nunca se acepta desde el cliente.
     */
    public PatientInsuranceResponse linkInsuranceForCurrentPatient(
            String username,
            PortalPatientInsuranceRequest request
    ) {
        Patient patient = requireLockedPatientProfile(username);
        Insurance insurance = insuranceRepository.findById(request.insuranceId())
                .orElseThrow(() -> new ResourceNotFoundException("Seguro", request.insuranceId()));

        if (!Boolean.TRUE.equals(insurance.getActive())) {
            throw new ValidationException("El seguro no está activo");
        }
        validatePortalDates(request.effectiveDate(), request.expirationDate());

        // Alcance del portal: cero o una póliza activa. El staff conserva sus
        // endpoints generales para escenarios administrativos más complejos.
        List<PatientInsurance> activeInsurances =
                patientInsuranceRepository.findByPatientAndActive(patient, true);
        if (!activeInsurances.isEmpty()) {
            throw new BusinessRuleException(
                    "PATIENT_INSURANCE_ALREADY_LINKED",
                    "Ya tienes una póliza activa; elimínala antes de registrar otra"
            );
        }

        PatientInsurance patientInsurance = patientInsuranceRepository
                .findByPatientAndInsurance(patient, insurance)
                .orElseGet(PatientInsurance::new);

        patientInsurance.setPatient(patient);
        patientInsurance.setInsurance(insurance);
        patientInsurance.setPolicyNumber(request.policyNumber().trim());
        patientInsurance.setPolicyHolderName(defaultHolderName(patient, request.policyHolderName()));
        patientInsurance.setRelationshipToHolder(defaultRelationship(request.relationshipToHolder()));
        patientInsurance.setIsPrimary(true);
        patientInsurance.setEffectiveDate(request.effectiveDate().atStartOfDay());
        patientInsurance.setExpirationDate(request.expirationDate().atTime(23, 59, 59));
        patientInsurance.setActive(true);
        if (patientInsurance.getUsedAnnualCoverage() == null) {
            patientInsurance.setUsedAnnualCoverage(BigDecimal.ZERO);
        }

        return mapToResponse(patientInsuranceRepository.save(patientInsurance));
    }

    /** Obtiene únicamente las pólizas activas del paciente autenticado. */
    @Transactional(readOnly = true)
    public List<PatientInsuranceResponse> getForCurrentPatient(String username) {
        Patient patient = requirePatientProfile(username);
        return patientInsuranceRepository.findByPatientAndActive(patient, true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Desvincula una póliza propia sin revelar si un ID ajeno existe.
     */
    public void unlinkInsuranceForCurrentPatient(String username, Long patientInsuranceId) {
        Patient patient = requireLockedPatientProfile(username);
        PatientInsurance patientInsurance = patientInsuranceRepository
                .findOwnedByIdForUpdate(patientInsuranceId, patient)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vinculación de seguro", patientInsuranceId
                ));

        patientInsurance.setActive(false);
        patientInsurance.setIsPrimary(false);
        patientInsuranceRepository.save(patientInsurance);
    }

    /**
     * Obtiene los seguros activos de un paciente
     */
    @Transactional(readOnly = true)
    public List<PatientInsuranceResponse> getPatientInsurances(Long patientId) {
        logger.debug("Obteniendo seguros del paciente: {}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", patientId));

        return patientInsuranceRepository.findByPatientAndActive(patient, true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene el seguro primario de un paciente
     * RN-25: Se usa este para calcular costos si existe
     */
    @Transactional(readOnly = true)
    public Optional<PatientInsuranceResponse> getPrimaryInsurance(Long patientId) {
        logger.debug("Obteniendo seguro primario del paciente: {}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", patientId));

        return patientInsuranceRepository.findByPatientAndIsPrimary(patient, true)
                .map(this::mapToResponse);
    }

    /**
     * Desvincula un seguro de un paciente
     */
    public void unlinkInsurance(Long patientInsuranceId) {
        logger.info("Desvinculando seguro: ID={}", patientInsuranceId);

        PatientInsurance patientInsurance = patientInsuranceRepository.findById(patientInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculación de seguro", patientInsuranceId));

        patientInsurance.setActive(false);
        patientInsurance.setUpdatedAt(LocalDateTime.now());
        patientInsuranceRepository.save(patientInsurance);

        logger.info("Seguro desvinculado: ID={}", patientInsuranceId);
    }

    private Patient requirePatientProfile(String username) {
        Patient patient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un perfil de paciente asociado al usuario autenticado"
                ));

        if (patient.getStatus() != UserStatus.ACTIVE
                || patient.getUser() == null
                || patient.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException(username);
        }
        return patient;
    }

    /**
     * Serializa altas de póliza para que dos solicitudes simultáneas no puedan
     * superar el límite de una póliza activa del portal.
     */
    private Patient requireLockedPatientProfile(String username) {
        Patient resolvedPatient = requirePatientProfile(username);
        Patient lockedPatient = patientRepository.findByIdForUpdate(resolvedPatient.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un perfil de paciente asociado al usuario autenticado"
                ));

        if (lockedPatient.getStatus() != UserStatus.ACTIVE
                || lockedPatient.getUser() == null
                || lockedPatient.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException(username);
        }
        return lockedPatient;
    }

    private void validatePortalDates(java.time.LocalDate effectiveDate, java.time.LocalDate expirationDate) {
        if (expirationDate.isBefore(effectiveDate)) {
            throw new ValidationException("La fecha de fin no puede ser anterior a la de inicio");
        }
        if (expirationDate.isBefore(java.time.LocalDate.now())) {
            throw new ValidationException("No puedes registrar una póliza que ya venció");
        }
    }

    private String defaultHolderName(Patient patient, String requestedName) {
        if (requestedName != null && !requestedName.isBlank()) {
            return requestedName.trim();
        }
        return (patient.getFirstName() + " " + patient.getLastName()).trim();
    }

    private String defaultRelationship(String requestedRelationship) {
        return requestedRelationship == null || requestedRelationship.isBlank()
                ? "SELF"
                : requestedRelationship.trim().toUpperCase();
    }

    /**
     * Mapea entidad a DTO
     */
    private PatientInsuranceResponse mapToResponse(PatientInsurance patientInsurance) {
        return new PatientInsuranceResponse(
            patientInsurance.getId(),
            patientInsurance.getPatient().getId(),
            patientInsurance.getInsurance().getId(),
            patientInsurance.getUsedAnnualCoverage(),
            patientInsurance.getInsurance().getName(),
            patientInsurance.getPolicyNumber(),
            patientInsurance.getPolicyHolderName(),
            patientInsurance.getRelationshipToHolder(),
            patientInsurance.getIsPrimary(),
            patientInsurance.getEffectiveDate(),
            patientInsurance.getExpirationDate(),
            patientInsurance.getActive(),
            patientInsurance.getCreatedAt(),
            patientInsurance.getUpdatedAt()
        );
    }
}
