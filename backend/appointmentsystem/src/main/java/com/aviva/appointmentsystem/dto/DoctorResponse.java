package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un doctor
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
