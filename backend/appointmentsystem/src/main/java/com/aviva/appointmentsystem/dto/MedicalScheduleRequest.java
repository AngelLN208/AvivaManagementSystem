package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO para crear/actualizar horarios médicos
 */
public record MedicalScheduleRequest(
    @NotNull(message = "El día de la semana es requerido")
    DayOfWeek dayOfWeek,

    @NotNull(message = "La hora de inicio es requerida")
    LocalTime startTime,

    @NotNull(message = "La hora de fin es requerida")
    LocalTime endTime,

    @NotNull(message = "La duración de cita es requerida")
    @Min(value = 15, message = "La duración mínima es 15 minutos")
    Integer appointmentDurationMinutes,

    @NotNull(message = "El máximo de citas es requerido")
    @Min(value = 1, message = "Debe permitir al menos 1 cita")
    Integer maxAppointmentsPerDay,

    String notes
) {}
