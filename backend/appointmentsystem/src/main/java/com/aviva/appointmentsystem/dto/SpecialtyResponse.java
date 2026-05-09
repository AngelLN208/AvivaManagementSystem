package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de una especialidad
 */
public record SpecialtyResponse(
    Long id,
    String name,
    String description,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
