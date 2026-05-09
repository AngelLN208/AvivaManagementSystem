package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear una cita
 */
public record AppointmentRequest(
    @NotNull(message = "El ID del paciente no puede estar vacío")
    Long patientId,

    @NotNull(message = "El ID del doctor no puede estar vacío")
    Long doctorId,

    @NotNull(message = "La fecha/hora de la cita no puede estar vacía")
    String appointmentDateTime,

    @Size(max = 500, message = "La razón no debe exceder 500 caracteres")
    String reason
) {
}
