package com.aviva.appointmentsystem.repository;

import com.aviva.appointmentsystem.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar notificaciones
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();
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

    /**
     * El portal se vincula por la relación cita-paciente, no por un correo
     * recibido desde el cliente. Así también conserva ownership si cambia el
     * correo del perfil después de crear la notificación.
     */
    @Query("""
        SELECT n FROM Notification n
        JOIN n.appointment a
        WHERE a.patient.id = :patientId
          AND n.channel = :channel
        ORDER BY n.createdAt DESC
        """)
    List<Notification> findPortalNotifications(
            @Param("patientId") Long patientId,
            @Param("channel") Notification.NotificationChannel channel
    );

    /** Devuelve una notificación únicamente si pertenece al paciente. */
    @Query("""
        SELECT n FROM Notification n
        JOIN n.appointment a
        WHERE n.id = :notificationId
          AND a.patient.id = :patientId
          AND n.channel = :channel
        """)
    Optional<Notification> findOwnedPortalNotification(
            @Param("notificationId") Long notificationId,
            @Param("patientId") Long patientId,
            @Param("channel") Notification.NotificationChannel channel
    );
}
