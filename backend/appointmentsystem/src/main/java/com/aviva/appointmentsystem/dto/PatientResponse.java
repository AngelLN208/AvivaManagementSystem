package com.aviva.appointmentsystem.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un paciente
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
