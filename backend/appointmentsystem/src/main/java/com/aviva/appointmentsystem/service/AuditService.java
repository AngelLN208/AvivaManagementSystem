package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.AuditLogResponse;
import com.aviva.appointmentsystem.entity.AuditLog;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestionar auditoría de cambios
 */
@Service
@Transactional
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Obtiene el historial de cambios de una cita
     * Ordenado por fecha de creación (más reciente primero)
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAppointmentHistory(Long appointmentId) {
        logger.debug("Obteniendo historial de auditoría para cita ID={}", appointmentId);

        // Validar que la cita exista (la validación se hace en el controlador)
        List<AuditLog> logs = auditLogRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);

        if (logs.isEmpty()) {
            logger.warn("No se encontró historial de auditoría para cita ID={}", appointmentId);
        }

        return logs.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene un registro de auditoría por ID
     */
    @Transactional(readOnly = true)
    public AuditLogResponse getById(Long id) {
        logger.debug("Obteniendo registro de auditoría ID={}", id);

        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de auditoría", id));

        return mapToResponse(auditLog);
    }

    /**
     * Mapea entidad a DTO
     */
    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return new AuditLogResponse(
            auditLog.getId(),
            auditLog.getAppointment().getId(),
            auditLog.getAction(),
            auditLog.getNewStatus().name(),
            auditLog.getDetails(),
            auditLog.getModifiedBy(),
            auditLog.getCreatedAt()
        );
    }
}
