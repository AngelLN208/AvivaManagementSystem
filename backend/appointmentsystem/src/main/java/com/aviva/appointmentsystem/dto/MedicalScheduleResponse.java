package com.aviva.appointmentsystem.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para un horario médico.
 * 
 * Devuelto en:
 * - GET /api/medical-schedules/doctor/{doctorId} (horarios de un doctor)
 * - GET /api/medical-schedules/doctor/{doctorId}/day (horarios por día)
 * - POST /api/medical-schedules/doctor/{doctorId} (creación)
 * - PUT /api/medical-schedules/{scheduleId} (actualización)
 * 
 * Incluye doctorId, doctorFirstName y doctorLastName para que el frontend
 * pueda mostrar a quién pertenece el horario sin hacer otra petición.
 * NUNCA se devuelve la entidad JPA directamente (prevención de recursión JSON).
 */
public record MedicalScheduleResponse(
    Long id,
    Long doctorId,
    String doctorFirstName,
    String doctorLastName,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    Integer appointmentDurationMinutes,
    Integer maxAppointmentsPerDay,
    Boolean active,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
