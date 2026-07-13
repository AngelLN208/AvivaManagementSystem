package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de notificaciones
 */
public record NotificationResponse(
    Long id,
    String type,
    String recipientEmail,
    String recipientName,
    Long appointmentId,
    String subject,
    String message,
    String channel,
    String status,
    Boolean read,
    LocalDateTime readAt,
    Integer retryCount,
    String errorMessage,
    LocalDateTime scheduledTime,
    LocalDateTime sentTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
