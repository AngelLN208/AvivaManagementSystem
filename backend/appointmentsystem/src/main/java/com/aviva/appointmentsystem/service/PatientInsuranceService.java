package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.PatientInsuranceRequest;
import com.aviva.appointmentsystem.dto.PatientInsuranceResponse;
import com.aviva.appointmentsystem.entity.Insurance;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.PatientInsurance;
import com.aviva.appointmentsystem.exception.BusinessRuleException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.InsuranceRepository;
import com.aviva.appointmentsystem.repository.PatientInsuranceRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final PatientInsuranceRepository patientInsuranceRepository;
    private final PatientRepository patientRepository;
    private final InsuranceRepository insuranceRepository;

    public PatientInsuranceService(PatientInsuranceRepository patientInsuranceRepository,
                                   PatientRepository patientRepository,
                                   InsuranceRepository insuranceRepository) {
        this.patientInsuranceRepository = patientInsuranceRepository;
        this.patientRepository = patientRepository;
        this.insuranceRepository = insuranceRepository;
    }

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

        // RN-08: No se puede duplicar la asociación (mismo paciente, mismo seguro)
        boolean alreadyLinked = patientInsuranceRepository
                .findByPatientAndInsuranceAndActive(patient, insurance, true)
                .isPresent();
        if (alreadyLinked) {
            throw new BusinessRuleException("RN-08_DUPLICATE",
                "El paciente ya tiene activa una póliza de '" + insurance.getName() + "'");
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
