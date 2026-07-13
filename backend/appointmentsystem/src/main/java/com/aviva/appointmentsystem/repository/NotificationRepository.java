package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para gestionar notificaciones
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatus(Notification.NotificationStatus status);
    List<Notification> findByScheduledTimeLessThanEqualAndStatusOrderByScheduledTimeAsc(
            LocalDateTime dateTime,
            Notification.NotificationStatus status
    );
    List<Notification> findByRecipientEmail(String recipientEmail);
    List<Notification> findByRecipientEmailAndChannelOrderByCreatedAtDesc(
            String recipientEmail,
            Notification.NotificationChannel channel
    );
    List<Notification> findByAppointmentId(Long appointmentId);
    List<Notification> findByStatusAndRetryCountLessThan(Notification.NotificationStatus status, Integer maxRetries);
}
