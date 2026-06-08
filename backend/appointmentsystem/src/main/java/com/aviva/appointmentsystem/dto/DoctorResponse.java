package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para un médico.
 * 
 * Devuelto en:
 * - GET /api/doctors (lista)
 * - GET /api/doctors/{id} (detalle)
 * - GET /api/doctors/by-specialty/{specialtyId} (por especialidad)
 * - POST /api/doctors (creación)
 * - PUT /api/doctors/{id} (actualización)
 * 
 * Incluye un SpecialtyResponse anidado para que el frontend 
 * NO tenga que hacer una segunda petición para obtener la especialidad.
 * NUNCA se devuelve la entidad JPA directamente (prevención de recursión JSON).
 */
public record DoctorResponse(
    Long id,
    String firstName,
    String lastName,
    String licenseNumber,
    String phone,
    String email,
    SpecialtyResponse specialty,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
