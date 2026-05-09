package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de una cita
 */
public record AppointmentResponse(
    Long id,
    PatientResponse patient,
    DoctorResponse doctor,
    LocalDateTime appointmentDateTime,
    String reason,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
