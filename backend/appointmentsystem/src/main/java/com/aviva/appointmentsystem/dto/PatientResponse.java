package com.aviva.appointmentsystem.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para un paciente.
 * 
 * Devuelto en:
 * - GET /api/patients (lista)
 * - GET /api/patients/{id} (detalle)
 * - GET /api/patients/search/dni (búsqueda por DNI)
 * - GET /api/patients/search (búsqueda por nombre)
 * - POST /api/patients (creación)
 * - PUT /api/patients/{id} (actualización)
 * - POST /api/auth/register-patient (auto-registro)
 * 
 * NUNCA se devuelve la entidad JPA directamente.
 */
public record PatientResponse(
    Long id,
    String dni,
    String firstName,
    String lastName,
    String gender,
    LocalDate dateOfBirth,
    String phone,
    String email,
    String address,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
