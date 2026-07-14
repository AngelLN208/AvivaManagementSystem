package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.NotificationResponse;
import com.aviva.appointmentsystem.dto.PatientNotificationResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.Notification;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.User;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.NotificationRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PatientRepository patientRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                patientRepository
        );
    }

    @Test
    void createEmailNotificationLeavesItPendingForScheduler() {
        returnSavedNotification();

        NotificationResponse response = notificationService.createNotification(
                Notification.NotificationType.APPOINTMENT_CREATED,
                "patient@example.com",
                "Paciente Prueba",
                null,
                "Cita creada",
                "Tu cita fue creada",
                Notification.NotificationChannel.EMAIL,
                null
        );

        assertEquals("EMAIL", response.channel());
        assertEquals("PENDING", response.status());
        assertNull(response.sentTime());
        assertFalse(response.read());
        assertNotNull(response.scheduledTime());
    }

    @Test
    void createInAppNotificationMakesItImmediatelyAvailable() {
        returnSavedNotification();

        NotificationResponse response = notificationService.createNotification(
                Notification.NotificationType.PAYMENT_RECEIVED,
                "patient@example.com",
                "Paciente Prueba",
                null,
                "Pago confirmado",
                "Tu pago fue confirmado",
                Notification.NotificationChannel.IN_APP,
                LocalDateTime.now()
        );

        assertEquals("IN_APP", response.channel());
        assertEquals("SENT", response.status());
        assertNotNull(response.sentTime());
        assertFalse(response.read());
    }

    @Test
    void markAsReadIsIdempotentAndKeepsFirstReadDate() {
        Notification notification = completeNotification(Notification.NotificationChannel.IN_APP);
        LocalDateTime firstReadAt = LocalDateTime.now().minusMinutes(5);
        notification.setRead(true);
        notification.setReadAt(firstReadAt);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markAsRead(10L);

        assertTrue(response.read());
        assertEquals(firstReadAt, response.readAt());
        verify(notificationRepository, never()).save(notification);
    }

    @Test
    void emailNotificationCannotBeMarkedAsRead() {
        Notification notification = completeNotification(Notification.NotificationChannel.EMAIL);
        when(notificationRepository.findById(11L)).thenReturn(Optional.of(notification));

        assertThrows(ValidationException.class, () -> notificationService.markAsRead(11L));
    }

    @Test
    void getAllNotificationsUsesRepositoryOrderForReceptionDashboard() {
        Notification notification = completeNotification(Notification.NotificationChannel.EMAIL);
        when(notificationRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(notification));

        List<NotificationResponse> response = notificationService.getAllNotifications();

        assertEquals(1, response.size());
        assertEquals(notification.getId(), response.getFirst().id());
    }

    @Test
    void patientPortalListsOnlyOwnedInAppNotifications() {
        Patient patient = activePatient(42L);
        Appointment appointment = new Appointment();
        appointment.setId(91L);
        appointment.setPatient(patient);
        Notification notification = completeNotification(
                Notification.NotificationChannel.IN_APP);
        notification.setAppointment(appointment);
        notification.setCreatedAt(LocalDateTime.now());

        when(patientRepository.findByUserUsername("patient.portal"))
                .thenReturn(Optional.of(patient));
        when(notificationRepository.findPortalNotifications(
                patient.getId(),
                Notification.NotificationChannel.IN_APP
        )).thenReturn(List.of(notification));

        List<PatientNotificationResponse> response =
                notificationService.getForCurrentPatient("patient.portal");

        assertEquals(1, response.size());
        assertEquals(91L, response.getFirst().appointmentId());
        assertFalse(response.getFirst().read());
    }

    @Test
    void patientPortalDoesNotRevealAnUnownedNotification() {
        Patient patient = activePatient(42L);
        when(patientRepository.findByUserUsername("patient.portal"))
                .thenReturn(Optional.of(patient));
        when(notificationRepository.findOwnedPortalNotification(
                99L,
                patient.getId(),
                Notification.NotificationChannel.IN_APP
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.markAsReadForCurrentPatient(
                        "patient.portal",
                        99L
                )
        );
    }

    private Notification completeNotification(Notification.NotificationChannel channel) {
        Notification notification = new Notification();
        notification.setId(10L);
        notification.setType(Notification.NotificationType.APPOINTMENT_CREATED);
        notification.setRecipientEmail("patient@example.com");
        notification.setRecipientName("Paciente Prueba");
        notification.setSubject("Cita creada");
        notification.setMessage("Tu cita fue creada");
        notification.setChannel(channel);
        notification.setStatus(Notification.NotificationStatus.SENT);
        notification.setRead(false);
        notification.setRetryCount(0);
        notification.setScheduledTime(LocalDateTime.now());
        return notification;
    }

    private void returnSavedNotification() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Patient activePatient(Long id) {
        User user = new User();
        user.setUsername("patient.portal");
        user.setStatus(UserStatus.ACTIVE);

        Patient patient = new Patient();
        patient.setId(id);
        patient.setUser(user);
        patient.setStatus(UserStatus.ACTIVE);
        return patient;
    }
}
