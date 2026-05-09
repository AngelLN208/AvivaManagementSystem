package com.aviva.appointmentsystem.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de horarios médicos
 */
public record MedicalScheduleResponse(
    Long id,
    Long doctorId,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    Integer appointmentDurationMinutes,
    Integer maxAppointmentsPerDay,
    Boolean active,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
