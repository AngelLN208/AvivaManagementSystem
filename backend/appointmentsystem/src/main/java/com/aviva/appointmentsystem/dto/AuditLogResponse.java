package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para registros de auditoría
 */
public record AuditLogResponse(
        Long id,
        Long appointmentId,
        String action,
        String newStatus,
        String details,
        String modifiedBy,
        LocalDateTime createdAt
) {}
