package com.aviva.appointmentsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

/**
 * Entidad para notificaciones del sistema
 * Registra correos enviados y notificaciones internas para los pacientes.
 * RF-45 a RF-48: Sistema de notificaciones por cambios en citas
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo de notificación
     * APPOINTMENT_CREATED, APPOINTMENT_RESCHEDULED, APPOINTMENT_CANCELLED, APPOINTMENT_REMINDER
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    /**
     * Receptor de la notificación
     * Actualmente corresponde al paciente relacionado con la operación.
     */
    @Column(nullable = false, length = 100)
    private String recipientEmail;

    /**
     * Nombre del receptor
     */
    @Column(nullable = false, length = 100)
    private String recipientName;

    /**
     * Cita asociada (si aplica)
     */
    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    /**
     * Asunto del correo/mensaje
     */
    @Column(nullable = false, length = 200)
    private String subject;

    /**
     * Cuerpo del mensaje
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Canal de envío
     * EMAIL o IN_APP
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    /**
     * Estado de la notificación
     * PENDING, SENT, FAILED, DELIVERED
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;

    /**
     * Número de intentos de envío
     */
    @Column(nullable = false)
    private Integer retryCount = 0;

    /**
     * Mensajes de error si falló
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Fecha de envío programado
     */
    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    /**
     * Fecha de envío real
     */
    @Column
    private LocalDateTime sentTime;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Indica si el paciente ya abrió la notificación interna.
     * Se utiliza únicamente para notificaciones del canal IN_APP.
     */
    @Column(name = "is_read", nullable = false)
    @ColumnDefault("false")
    private Boolean read = false;

    /**
     * Fecha y hora en la que el paciente leyó la notificación.
     * Permanece nula mientras no haya sido abierta.
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    
    public LocalDateTime getSentTime() { return sentTime; }
    public void setSentTime(LocalDateTime sentTime) { this.sentTime = sentTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }       

    /**
     * Enumeración para tipos de notificación
     */
    public enum NotificationType {
        APPOINTMENT_CREATED("Cita creada"),
        APPOINTMENT_RESCHEDULED("Cita reprogramada"),
        APPOINTMENT_CANCELLED("Cita cancelada"),
        APPOINTMENT_REMINDER("Recordatorio de cita"),
        APPOINTMENT_CONFIRMED("Cita confirmada"),
        APPOINTMENT_UPDATED("Cita modificada"),
        PAYMENT_RECEIVED("Pago recibido"),
        PAYMENT_FAILED("Fallo en pago"),
        TREATMENT_PLAN("Plan de tratamiento");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enumeración para canales de notificación
     */
    public enum NotificationChannel {
        EMAIL,
        IN_APP
    }

    /**
     * Enumeración para estados de notificación
     */
    public enum NotificationStatus {
        PENDING,      // Pendiente de envío
        SENT,         // Enviado pero no confirmado
        DELIVERED,    // Entregado exitosamente
        FAILED        // Falló el envío
    }
}
