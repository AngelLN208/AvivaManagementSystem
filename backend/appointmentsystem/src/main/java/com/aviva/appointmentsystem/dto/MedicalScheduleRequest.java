package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO para crear un horario médico.
 * 
 * Usado en: POST /api/medical-schedules/doctor/{doctorId}
 * 
 * Validaciones:
 * - dayOfWeek: obligatorio (MONDAY..SUNDAY)
 * - startTime: obligatorio (ej: "08:00")
 * - endTime: obligatorio (ej: "17:00")
 * - appointmentDurationMinutes: obligatorio, mínimo 15
 * - maxAppointmentsPerDay: obligatorio, mínimo 1
 * 
 * Validación cruzada (en el Service, no aquí):
 * - startTime DEBE ser estrictamente anterior a endTime
 * - No debe haber cruces de horario con horarios existentes del mismo doctor
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

    @NotNull(message = "El máximo de citas por día es requerido")
    @Min(value = 1, message = "Debe permitir al menos 1 cita por día")
    Integer maxAppointmentsPerDay,

    String notes

) {
}
