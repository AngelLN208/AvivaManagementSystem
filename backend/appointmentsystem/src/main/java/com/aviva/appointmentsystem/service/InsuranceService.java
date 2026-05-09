package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.InsuranceRequest;
import com.aviva.appointmentsystem.dto.InsuranceResponse;
import com.aviva.appointmentsystem.entity.Insurance;
import com.aviva.appointmentsystem.exception.ResourceAlreadyExistsException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.InsuranceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar seguros médicos
 * RN-04 a RN-08: Reglas de validación de convenios
 */
@Service
@Transactional
public class InsuranceService {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceService.class);

    @Autowired
    private InsuranceRepository insuranceRepository;

    /**
     * Crea un nuevo seguro
     */
    public InsuranceResponse create(InsuranceRequest request) {
        logger.info("Creando nuevo seguro: {}", request.name());

        // RN-04: Validar que no exista otro con el mismo nombre
        insuranceRepository.findByName(request.name()).ifPresent(existing -> {
            logger.warn("Intento de crear seguro duplicado: {}", request.name());
            throw new ResourceAlreadyExistsException("Seguro con nombre: " + request.name());
        });

        Insurance insurance = new Insurance();
        insurance.setName(request.name());
        insurance.setDescription(request.description());
        insurance.setCoveragePercentage(request.coveragePercentage());
        insurance.setDeductible(request.deductible());
        insurance.setMaxCoveragePerConsultation(request.maxCoveragePerConsultation());
        insurance.setMaxAnnualCoverage(request.maxAnnualCoverage());
        insurance.setUsedAnnualCoverage(java.math.BigDecimal.ZERO);
        insurance.setActive(true);
        insurance.setRequiresPreAuthorization(request.requiresPreAuthorization() != null && request.requiresPreAuthorization());
        insurance.setCreatedAt(LocalDateTime.now());
        insurance.setUpdatedAt(LocalDateTime.now());

        Insurance saved = insuranceRepository.save(insurance);
        logger.info("Seguro creado exitosamente: ID={}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Actualiza un seguro
     */
    public InsuranceResponse update(Long id, InsuranceRequest request) {
        logger.info("Actualizando seguro: ID={}", id);

        Insurance insurance = insuranceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seguro", id));

        // Validar nombre único si cambió
        if (!insurance.getName().equals(request.name())) {
            insuranceRepository.findByName(request.name()).ifPresent(existing -> {
                throw new ResourceAlreadyExistsException("Seguro con nombre: " + request.name());
            });
        }

        insurance.setName(request.name());
        insurance.setDescription(request.description());
        insurance.setCoveragePercentage(request.coveragePercentage());
        insurance.setDeductible(request.deductible());
        insurance.setMaxCoveragePerConsultation(request.maxCoveragePerConsultation());
        insurance.setMaxAnnualCoverage(request.maxAnnualCoverage());
        insurance.setRequiresPreAuthorization(request.requiresPreAuthorization() != null && request.requiresPreAuthorization());
        insurance.setUpdatedAt(LocalDateTime.now());

        Insurance updated = insuranceRepository.save(insurance);
        logger.info("Seguro actualizado: ID={}", id);

        return mapToResponse(updated);
    }

    /**
     * Obtiene un seguro por ID
     */
    @Transactional(readOnly = true)
    public InsuranceResponse getById(Long id) {
        logger.debug("Obteniendo seguro: ID={}", id);

        Insurance insurance = insuranceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seguro", id));

        return mapToResponse(insurance);
    }

    /**
     * Lista todos los seguros activos
     */
    @Transactional(readOnly = true)
    public List<InsuranceResponse> getAll() {
        logger.debug("Obteniendo todos los seguros");

        return insuranceRepository.findByActive(true)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Desactiva un seguro
     */
    public void deactivate(Long id) {
        logger.info("Desactivando seguro: ID={}", id);

        Insurance insurance = insuranceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seguro", id));

        insurance.setActive(false);
        insurance.setUpdatedAt(LocalDateTime.now());
        insuranceRepository.save(insurance);

        logger.info("Seguro desactivado: ID={}", id);
    }

    /**
     * Mapea entidad a DTO
     */
    private InsuranceResponse mapToResponse(Insurance insurance) {
        return new InsuranceResponse(
            insurance.getId(),
            insurance.getName(),
            insurance.getDescription(),
            insurance.getCoveragePercentage(),
            insurance.getDeductible(),
            insurance.getMaxCoveragePerConsultation(),
            insurance.getMaxAnnualCoverage(),
            insurance.getUsedAnnualCoverage(),
            insurance.getActive(),
            insurance.getRequiresPreAuthorization(),
            insurance.getCreatedAt(),
            insurance.getUpdatedAt()
        );
    }
}
