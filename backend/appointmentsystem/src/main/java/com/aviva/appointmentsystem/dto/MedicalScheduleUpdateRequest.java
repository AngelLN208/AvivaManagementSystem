package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.Min;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO para actualización parcial de un horario médico.
 * 
 * Usado en: PUT /api/medical-schedules/{scheduleId}
 * 
 * Todos los campos son opcionales (nullable).
 * El Service aplicará solo los campos presentes (no nulos).
 * 
 * Ejemplo del Postman: { "endTime": "18:00", "appointmentDurationMinutes": 30 }
 * → Solo se actualizan esos 2 campos.
 * 
 * Validación cruzada (en el Service): si se envía startTime y/o endTime,
 * el Service valida que el resultado final tenga startTime < endTime.
 */
public record MedicalScheduleUpdateRequest(

    DayOfWeek dayOfWeek,

    LocalTime startTime,

    LocalTime endTime,

    @Min(value = 15, message = "La duración mínima es 15 minutos")
    Integer appointmentDurationMinutes,

    @Min(value = 1, message = "Debe permitir al menos 1 cita por día")
    Integer maxAppointmentsPerDay,

    String notes

) {
}
