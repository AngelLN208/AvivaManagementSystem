package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * Vista mínima de una notificación interna para el portal paciente.
 * Omite correo, reintentos y errores técnicos propios del panel de staff.
 */
public record PatientNotificationResponse(
        Long id,
        String type,
        Long appointmentId,
        String subject,
        String message,
        Boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {}
