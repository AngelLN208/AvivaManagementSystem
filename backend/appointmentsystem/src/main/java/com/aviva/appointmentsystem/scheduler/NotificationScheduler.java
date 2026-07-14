package com.aviva.appointmentsystem.scheduler;

import com.aviva.appointmentsystem.entity.Notification;
import com.aviva.appointmentsystem.service.EmailContent;
import com.aviva.appointmentsystem.service.EmailSender;
import com.aviva.appointmentsystem.service.EmailTemplateService;
import com.aviva.appointmentsystem.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Procesa periódicamente las notificaciones de correo pendientes.
 *
 * Las notificaciones internas no pasan por este scheduler porque quedan
 * disponibles inmediatamente en la base de datos.
 */
@Component
@ConditionalOnProperty(
        name = "app.notifications.scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationService notificationService;
    private final EmailSender emailSender;
    private final EmailTemplateService emailTemplateService;

    public NotificationScheduler(
            NotificationService notificationService,
            EmailSender emailSender,
            EmailTemplateService emailTemplateService
    ) {
        this.notificationService = notificationService;
        this.emailSender = emailSender;
        this.emailTemplateService = emailTemplateService;
    }

    /**
     * Busca correos pendientes cada minuto.
     *
     * fixedDelay espera un minuto después de terminar la ejecución anterior,
     * evitando que dos ejecuciones del mismo proceso se superpongan.
     */
    @Scheduled(
        fixedDelayString =
            "${app.notifications.processing-delay-ms:60000}"
    )
    public void processPendingNotifications() {
        List<Notification> pendingNotifications =
                notificationService.getPendingNotifications();

        if (pendingNotifications.isEmpty()) {
            logger.debug("No hay correos pendientes");
            return;
        }

        logger.info(
                "Procesando {} correos pendientes",
                pendingNotifications.size()
        );

        for (Notification notification : pendingNotifications) {
            processNotification(notification);
        }
    }

    /**
     * Envía una notificación de correo individual y actualiza su estado.
     */
    private void processNotification(Notification notification) {
        if (notification.getChannel()
                != Notification.NotificationChannel.EMAIL) {
            logger.warn(
                    "Notificación pendiente con canal no procesable: ID={}",
                    notification.getId()
            );
            return;
        }

        try {
            EmailContent email = emailTemplateService.notification(notification);
            emailSender.send(
                    notification.getRecipientEmail(),
                    email.subject(),
                    email.textContent(),
                    email.htmlContent()
            );

            notificationService.markAsSent(notification.getId());

            logger.info(
                    "Correo enviado correctamente: ID={}",
                    notification.getId()
            );
        } catch (Exception exception) {
            String errorMessage = exception.getMessage() != null
                    ? exception.getMessage()
                    : exception.getClass().getSimpleName();

            logger.error(
                    "No se pudo enviar el correo: ID={}",
                    notification.getId(),
                    exception
            );

            notificationService.markAsFailed(
                    notification.getId(),
                    errorMessage
            );
        }
    }
}
