package com.aviva.appointmentsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Datos que un paciente autenticado puede enviar para crear su propia cita.
 *
 * No incluye patientId deliberadamente: el backend obtiene al paciente desde
 * la identidad del JWT y evita que el cliente suplante a otro paciente.
 */
public record PatientAppointmentRequest(

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
