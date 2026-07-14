package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.NotificationResponse;
import com.aviva.appointmentsystem.dto.PatientNotificationResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.Notification;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.UserInactiveException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.NotificationRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final NotificationRepository notificationRepository;
    private final PatientRepository patientRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            PatientRepository patientRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Crea y registra una notificación para un canal específico.
     *
     * Las notificaciones EMAIL se guardan como PENDING para que posteriormente
     * sean procesadas por el scheduler y enviadas mediante la API HTTPS de Brevo.
     *
     * Las notificaciones IN_APP se guardan como SENT porque quedan disponibles
     * inmediatamente en la base de datos para el portal del paciente.
     *
     * @param type tipo de evento que originó la notificación
     * @param recipientEmail correo del paciente destinatario
     * @param recipientName nombre completo del paciente
     * @param appointment cita relacionada con la notificación
     * @param subject asunto que se mostrará en el correo o portal
     * @param message contenido de la notificación
     * @param channel canal por el que se entregará la notificación
     * @param scheduledTime fecha y hora programada para su procesamiento
     * @return información de la notificación registrada
     */
    public NotificationResponse createNotification(
            Notification.NotificationType type,
            String recipientEmail,
            String recipientName,
            Appointment appointment,
            String subject,
            String message,
            Notification.NotificationChannel channel,
            LocalDateTime scheduledTime
    ) {
        logger.info(
                "Creando notificación: tipo={}, canal={}, destinatario={}",
                type,
                channel,
                recipientEmail
        );

        LocalDateTime now = LocalDateTime.now();

        Notification notification = new Notification();
        notification.setType(type);
        notification.setRecipientEmail(recipientEmail);
        notification.setRecipientName(recipientName);
        notification.setAppointment(appointment);
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setChannel(channel);
        notification.setRetryCount(0);
        notification.setErrorMessage(null);
        notification.setRead(false);
        notification.setReadAt(null);

        /*
        * scheduledTime no puede ser nulo en la entidad. Cuando el llamador no
        * proporciona una fecha, la notificación se programa inmediatamente.
        */
        notification.setScheduledTime(
                scheduledTime != null ? scheduledTime : now
        );

        /*
        * Una notificación interna ya está disponible desde el momento en que
        * se guarda. El correo, en cambio, debe esperar a que el scheduler lo
        * envíe utilizando la API de correo de Brevo.
        */
        if (channel == Notification.NotificationChannel.IN_APP) {
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentTime(now);
        } else {
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setSentTime(null);
        }

        Notification savedNotification =
                notificationRepository.save(notification);

        logger.info(
                "Notificación creada: ID={}, estado={}, canal={}",
                savedNotification.getId(),
                savedNotification.getStatus(),
                savedNotification.getChannel()
        );

        return mapToResponse(savedNotification);
    }

    /**
     * Obtiene notificaciones pendientes para envío
     * Las que han llegado su hora de envío
     */
    @Transactional(readOnly = true)
    public List<Notification> getPendingNotifications() {
        logger.debug("Obteniendo notificaciones pendientes");

        return notificationRepository.findByScheduledTimeLessThanEqualAndStatusOrderByScheduledTimeAsc(
            LocalDateTime.now(),
            Notification.NotificationStatus.PENDING
        );
    }

    /**
     * Marca una notificación de correo como enviada correctamente.
     *
     * Este método se ejecuta después de que la API de Brevo acepta el correo.
     * No utiliza el estado DELIVERED porque no es posible garantizar que el
     * paciente haya abierto o leído el mensaje.
     *
     * @param notificationId identificador de la notificación enviada
     */
    public void markAsSent(Long notificationId) {
        logger.info(
                "Marcando notificación como enviada: ID={}",
                notificationId
        );

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notificación",
                                notificationId
                        )
                );

        notification.setStatus(Notification.NotificationStatus.SENT);
        notification.setSentTime(LocalDateTime.now());
        notification.setErrorMessage(null);

        notificationRepository.save(notification);

        logger.info(
                "Notificación marcada como enviada: ID={}, fecha={}",
                notificationId,
                notification.getSentTime()
        );
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
     * Lista el historial general de notificaciones para el panel de recepcion.
     * Incluye EMAIL e IN_APP para facilitar el seguimiento de envios y fallos.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene solo las notificaciones internas del paciente, ordenadas desde
     * la mas reciente. Este listado es el que consumira el portal web.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserInAppNotifications(String email) {
        return notificationRepository
                .findByRecipientEmailAndChannelOrderByCreatedAtDesc(
                        email,
                        Notification.NotificationChannel.IN_APP
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lista las notificaciones internas del paciente autenticado. El username
     * proviene del JWT y el repositorio valida ownership mediante la cita.
     */
    @Transactional(readOnly = true)
    public List<PatientNotificationResponse> getForCurrentPatient(String username) {
        Patient patient = requirePatientProfile(username);
        return notificationRepository.findPortalNotifications(
                        patient.getId(),
                        Notification.NotificationChannel.IN_APP
                )
                .stream()
                .map(this::mapToPatientResponse)
                .toList();
    }

    /**
     * Marca como leida una notificacion interna. La operacion es idempotente:
     * una segunda llamada conserva la fecha de la primera lectura.
     */
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", notificationId));

        return mapToResponse(markInternalAsRead(notification));
    }

    /**
     * Marca una notificación propia como leída. Una notificación inexistente y
     * una ajena producen la misma respuesta 404 para no revelar recursos.
     */
    public PatientNotificationResponse markAsReadForCurrentPatient(
            String username,
            Long notificationId
    ) {
        Patient patient = requirePatientProfile(username);
        Notification notification = notificationRepository
                .findOwnedPortalNotification(
                        notificationId,
                        patient.getId(),
                        Notification.NotificationChannel.IN_APP
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificación",
                        notificationId
                ));

        return mapToPatientResponse(markInternalAsRead(notification));
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
            notification.getRead(),
            notification.getReadAt(),
            notification.getRetryCount(),
            notification.getErrorMessage(),
            notification.getScheduledTime(),
            notification.getSentTime(),
            notification.getCreatedAt(),
            notification.getUpdatedAt()
            
        );
    }

    private PatientNotificationResponse mapToPatientResponse(Notification notification) {
        return new PatientNotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getAppointment() != null
                        ? notification.getAppointment().getId()
                        : null,
                notification.getSubject(),
                notification.getMessage(),
                Boolean.TRUE.equals(notification.getRead()),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }

    private Notification markInternalAsRead(Notification notification) {
        if (notification.getChannel() != Notification.NotificationChannel.IN_APP) {
            throw new ValidationException(
                    "Solo las notificaciones internas pueden marcarse como leídas"
            );
        }

        if (!Boolean.TRUE.equals(notification.getRead())) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            return notificationRepository.save(notification);
        }

        return notification;
    }

    private Patient requirePatientProfile(String username) {
        Patient patient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un perfil de paciente asociado al usuario autenticado"
                ));

        if (patient.getStatus() != UserStatus.ACTIVE
                || patient.getUser() == null
                || patient.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException(username);
        }

        return patient;
    }
}
