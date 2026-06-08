package com.aviva.appointmentsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para crear una cita médica.
 *
 * Usado en: POST /api/appointments
 *
 * Validaciones:
 * - patientId: obligatorio
 * - doctorId: obligatorio
 * - appointmentDateTime: obligatorio, debe ser fecha futura, formato "yyyy-MM-ddTHH:mm:ss"
 * - reason: opcional, máximo 500 caracteres
 *
 * Validaciones cruzadas (en AppointmentService):
 * - RN-38: La hora debe caer dentro del horario activo del doctor ese día
 * - RN-12: No debe existir otra cita activa del doctor en ese datetime exacto
 * - RN-13: El estado inicial siempre es PENDING
 */
public record AppointmentRequest(

    @NotNull(message = "El ID del paciente es obligatorio")
    Long patientId,

    @NotNull(message = "El ID del doctor es obligatorio")
    Long doctorId,

    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    @Future(message = "La fecha y hora de la cita debe ser en el futuro")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime appointmentDateTime,

    @Size(max = 500, message = "La razón no debe exceder 500 caracteres")
    String reason

) {
}
