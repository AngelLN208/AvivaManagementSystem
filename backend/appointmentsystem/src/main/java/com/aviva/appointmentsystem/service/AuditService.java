package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.AuditLogResponse;
import com.aviva.appointmentsystem.entity.AuditLog;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de solo lectura para trazabilidad de auditoría.
 *
 * RN-42/RN-43: Los registros de auditoría se crean automáticamente en AppointmentService
 * y PaymentService cuando ocurren acciones clave (CREATED, RESCHEDULED, CANCELLED,
 * PAYMENT_CONFIRMED). Este servicio SOLO expone endpoints de consulta.
 *
 * NUNCA se exponen endpoints de creación, modificación o eliminación de audit logs.
 * La inmutabilidad es una garantía de integridad del sistema.
 *
 * Inyección: Constructor injection (no @Autowired).
 */
@Service
@Transactional(readOnly = true)
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Obtiene el historial completo de auditoría de una cita.
     * Ordenado por fecha de creación descendente (más reciente primero).
     *
     * @param appointmentId ID de la cita
     * @return lista de registros de auditoría ordenados por fecha DESC
     */
    public List<AuditLogResponse> getAppointmentHistory(Long appointmentId) {
        logger.debug("Obteniendo historial de auditoría para cita ID={}", appointmentId);

        List<AuditLog> logs = auditLogRepository
                .findByAppointmentIdOrderByCreatedAtDesc(appointmentId);

        if (logs.isEmpty()) {
            logger.debug("Sin registros de auditoría para cita ID={}", appointmentId);
        }

        return logs.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene un registro de auditoría individual por su ID.
     *
     * @param id ID del registro de auditoría
     * @throws ResourceNotFoundException si no existe
     */
    public AuditLogResponse getById(Long id) {
        logger.debug("Obteniendo registro de auditoría ID={}", id);

        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de auditoría", id));

        return mapToResponse(auditLog);
    }

    // ========================================================
    // MAPEO Entity → DTO
    // ========================================================

    /**
     * Mapea AuditLog a AuditLogResponse.
     * NUNCA devuelve la entidad AuditLog directamente.
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
