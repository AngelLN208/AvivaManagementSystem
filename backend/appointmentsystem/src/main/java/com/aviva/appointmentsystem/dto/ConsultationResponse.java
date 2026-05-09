package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de consulta
 */
public record ConsultationResponse(
    Long id,
    Long appointmentId,
    String diagnosis,
    String treatment,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
