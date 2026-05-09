package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.NotificationResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.Notification;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar notificaciones
 * RF-45 a RF-48: Sistema de notificaciones por cambios en citas
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Crea una notificación pendiente
     * Se programa para envío asincrónico
     */
    public NotificationResponse createNotification(
            Notification.NotificationType type,
            String recipientEmail,
            String recipientName,
            Appointment appointment,
            String subject,
            String message,
            Notification.NotificationChannel channel,
            LocalDateTime scheduledTime) {

        logger.info("Creando notificación: tipo={}, destinatario={}", type, recipientEmail);

        Notification notification = new Notification();
        notification.setType(type);
        notification.setRecipientEmail(recipientEmail);
        notification.setRecipientName(recipientName);
        notification.setAppointment(appointment);
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setChannel(channel);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setScheduledTime(scheduledTime);
        notification.setRetryCount(0);

        Notification saved = notificationRepository.save(notification);
        logger.info("Notificación creada: ID={}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Obtiene notificaciones pendientes para envío
     * Las que han llegado su hora de envío
     */
    @Transactional(readOnly = true)
    public List<Notification> getPendingNotifications() {
        logger.debug("Obteniendo notificaciones pendientes");

        return notificationRepository.findByScheduledTimeBeforeAndStatus(
            LocalDateTime.now(),
            Notification.NotificationStatus.PENDING
        );
    }

    /**
     * Marca una notificación como enviada
     */
    public void markAsSent(Long notificationId) {
        logger.info("Marcando notificación como enviada: ID={}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", notificationId));

        notification.setStatus(Notification.NotificationStatus.DELIVERED);
        notification.setSentTime(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        logger.info("Notificación marcada como enviada: ID={}", notificationId);
    }

    /**
     * Registra fallo en el envío
     */
    public void markAsFailed(Long notificationId, String errorMessage) {
        logger.warn("Registrando fallo en notificación: ID={}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", notificationId));

        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setErrorMessage(errorMessage);

        // Si alcanzó máximo de reintentos (3), marcar como fallida
        if (notification.getRetryCount() >= 3) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            logger.error("Notificación marcada como fallida tras 3 intentos: ID={}", notificationId);
        }

        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * Obtiene notificaciones de un paciente
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(String email) {
        logger.debug("Obteniendo notificaciones del usuario: {}", email);

        return notificationRepository.findByRecipientEmail(email)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene notificaciones de una cita
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAppointmentNotifications(Long appointmentId) {
        logger.debug("Obteniendo notificaciones de cita: ID={}", appointmentId);

        return notificationRepository.findByAppointmentId(appointmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Mapea entidad a DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getType().name(),
            notification.getRecipientEmail(),
            notification.getRecipientName(),
            notification.getAppointment() != null ? notification.getAppointment().getId() : null,
            notification.getSubject(),
            notification.getMessage(),
            notification.getChannel().name(),
            notification.getStatus().name(),
            notification.getRetryCount(),
            notification.getErrorMessage(),
            notification.getScheduledTime(),
            notification.getSentTime(),
            notification.getCreatedAt(),
            notification.getUpdatedAt()
        );
    }
}
