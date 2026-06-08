package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para una especialidad médica.
 * 
 * Devuelto en:
 * - GET /api/specialties (lista)
 * - GET /api/specialties/{id} (detalle)
 * - POST /api/specialties (creación)
 * - PUT /api/specialties/{id} (actualización)
 * 
 * NUNCA se devuelve la entidad JPA directamente.
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
