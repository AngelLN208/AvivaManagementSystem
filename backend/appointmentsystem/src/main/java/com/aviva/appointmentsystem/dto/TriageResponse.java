package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de triaje
 */
public record TriageResponse(
    Long id,
    Integer bloodPressureSystolic,
    Integer bloodPressureDiastolic,
    Double temperature,
    Integer heartRate,
    Integer respiratoryRate,
    Double weight,
    Double height,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
