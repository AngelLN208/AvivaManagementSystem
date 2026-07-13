package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.PaymentResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.entity.AuditLog;
import com.aviva.appointmentsystem.entity.Notification;
import com.aviva.appointmentsystem.entity.PatientInsurance;
import com.aviva.appointmentsystem.entity.Payment;
import com.aviva.appointmentsystem.entity.PaymentMethod;
import com.aviva.appointmentsystem.entity.PaymentStatus;
import com.aviva.appointmentsystem.entity.Receipt;
import com.aviva.appointmentsystem.exception.BusinessRuleException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.AuditLogRepository;
import com.aviva.appointmentsystem.repository.PatientInsuranceRepository;
import com.aviva.appointmentsystem.repository.PaymentRepository;
import com.aviva.appointmentsystem.repository.ReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para la gestión de pagos.
 *
 * Flujo principal — processPayment (POST /api/payments/{paymentId}/process):
 *
 * 1. Obtener Payment por ID (no por appointmentId)
 * 2. Validar que el pago esté en estado PENDING
 *    - Si PAID  → BusinessRuleException (RN-26: ya procesado)
 *    - Si CANCELLED → BusinessRuleException
 * 3. RN-26: Actualizar Payment → PAID, setear method y paymentDate
 * 4. RN-16: Confirmar la Appointment asociada → CONFIRMED
 * 5. Registrar en AuditLog (acción PAYMENT_CONFIRMED)
 * 6. RN-28: Generar Receipt automáticamente con número único
 * 7. Notificar al paciente (wrapped en try-catch)
 *
 * Inyección: Constructor injection (no @Autowired).
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    private final PatientInsuranceRepository patientInsuranceRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReceiptRepository receiptRepository,
            AppointmentRepository appointmentRepository,
            AuditLogRepository auditLogRepository,
            NotificationService notificationService,
            PatientInsuranceRepository patientInsuranceRepository) {

        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
        this.patientInsuranceRepository = patientInsuranceRepository;
    }

    // ========================================================
    // OPERACIÓN PRINCIPAL
    // ========================================================

    /**
     * Procesa un pago por su ID.
     *
     * Recibe el ID del Payment (generado automáticamente al crear la cita — RN-21),
     * NO el ID de la Appointment. El cliente primero consulta GET /api/payments/appointment/{id}
     * para obtener el paymentId, luego llama a este endpoint.
     *
     * Cadena de eventos transaccional:
     * Payment(PENDING→PAID) → Appointment(PENDING/RESCHEDULED→CONFIRMED) → AuditLog → Receipt → Notification
     *
     * @param paymentId ID del pago a procesar
     * @param method    método de pago (CASH, CREDIT_CARD, DEBIT_CARD, TRANSFER, INSURANCE)
     * @return datos del pago procesado
     */
    public PaymentResponse processPayment(Long paymentId, PaymentMethod method) {
        logger.info("Procesando pago ID={} con método={}", paymentId, method);

        // 1. Obtener el pago
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", paymentId));

        // 2. Validar estado del pago
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BusinessRuleException("RN-26_ALREADY_PAID",
                "El pago ID=" + paymentId + " ya fue procesado anteriormente");
        }
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessRuleException("RN-26_CANCELLED",
                "No se puede procesar un pago cancelado");
        }

        // 3. RN-26: Actualizar pago → PAID
        LocalDateTime now = LocalDateTime.now();
        payment.setStatus(PaymentStatus.PAID);
        payment.setMethod(method);
        payment.setPaymentDate(now);
        payment.setUpdatedAt(now);
        Payment savedPayment = paymentRepository.save(payment);
        // Registrar el consumo del seguro después de confirmar el pago.
        updateAnnualInsuranceCoverage(savedPayment, now);
        logger.info("RN-26: Pago ID={} marcado como PAID", paymentId);

        // 4. RN-16: Confirmar la cita asociada → CONFIRMED
        Appointment appointment = savedPayment.getAppointment();
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(now);
        appointmentRepository.save(appointment);
        logger.info("RN-16: Cita ID={} confirmada tras pago", appointment.getId());

        // 5. Registrar en AuditLog
        registerAudit(appointment, "PAYMENT_CONFIRMED",
            String.format("Pago ID=%d procesado con método %s. Cita confirmada.", paymentId, method),
            "SYSTEM");

        // 6. RN-28: Generar Receipt automáticamente
        Receipt receipt = generateReceipt(savedPayment);
        logger.info("RN-28: Comprobante generado: {}", receipt.getReceiptNumber());

        // 7. Notificar al paciente
        notifyPaymentConfirmed(appointment, receipt.getReceiptNumber());

        return mapToResponse(savedPayment);
    }

    // ========================================================
    // CONSULTAS
    // ========================================================

    /**
     * Obtiene un pago por su ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", id));
        return mapToResponse(payment);
    }

    /**
     * Obtiene los pagos asociados a una cita.
     * En la práctica siempre habrá uno (creado en RN-21), pero se devuelve lista
     * por flexibilidad ante pagos parciales o múltiples intentos.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByAppointment(Long appointmentId) {
        logger.debug("Obteniendo pagos de cita ID={}", appointmentId);
        return paymentRepository.findByAppointmentId(appointmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lista todos los pagos del sistema.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lista pagos filtrados por estado.
     *
     * @param status PaymentStatus (PENDING, PAID, CANCELLED, REFUNDED)
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ========================================================
    // OPERACIONES AUXILIARES PRIVADAS
    // ========================================================

    /**
     * RN-28: Genera un comprobante de pago con número único.
     *
     * Formato del número: RCP-{yyyyMMdd}-{8 chars UUID uppercase}
     * Ejemplo: RCP-20260520-A3F7B2C1
     */
    private Receipt generateReceipt(Payment payment) {
        String receiptNumber = "RCP-"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            + "-"
            + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Receipt receipt = new Receipt();
        receipt.setPayment(payment);
        receipt.setReceiptNumber(receiptNumber);
        receipt.setTotalAmount(payment.getAmount());
        receipt.setDescription(String.format(
            "Comprobante por consulta médica — Cita ID=%d",
            payment.getAppointment().getId()));
        receipt.setCreatedAt(LocalDateTime.now());

        return receiptRepository.save(receipt);
    }

    /**
     * Registra en la afiliacion del paciente el monto cubierto por el seguro.
     *
     * No realiza ninguna operacion cuando la cita fue particular o cuando
     * la aseguradora no cubrio parte del costo.
     *
     * @param payment pago confirmado que contiene el desglose de cobertura
     * @param updateTime momento en que se registra el consumo
     */
    private void updateAnnualInsuranceCoverage(
            Payment payment,
            LocalDateTime updateTime) {

        PatientInsurance policy = payment.getPatientInsurance();
        BigDecimal coveredAmount = payment.getInsuranceCoveredAmount();

        if (policy == null ||
                coveredAmount == null ||
                coveredAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal currentUsage = policy.getUsedAnnualCoverage() != null
                ? policy.getUsedAnnualCoverage()
                : BigDecimal.ZERO;

        policy.setUsedAnnualCoverage(currentUsage.add(coveredAmount));
        policy.setUpdatedAt(updateTime);

        patientInsuranceRepository.save(policy);
    }

    /**
     * Registra un evento en el log de auditoría de la cita.
     * Wrapped en try-catch: una falla de auditoría no debe revertir el pago.
     */
    private void registerAudit(Appointment appointment, String action, String details, String modifiedBy) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAppointment(appointment);
            auditLog.setAction(action);
            auditLog.setNewStatus(appointment.getStatus());
            auditLog.setDetails(details);
            auditLog.setModifiedBy(modifiedBy);
            auditLog.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Error registrando auditoría para cita ID={}: {}", appointment.getId(), e.getMessage());
        }
    }

    /**
     * Notifica al paciente que el pago fue confirmado.
     * Wrapped en try-catch: una falla de notificación no revierte el pago.
     */
    private void notifyPaymentConfirmed(Appointment appointment, String receiptNumber) {
        try {
            String message = String.format(
                "Tu pago para la cita del %s ha sido confirmado. Comprobante: %s. " +
                "Tu cita está CONFIRMADA.",
                appointment.getAppointmentDateTime().toLocalDate(),
                receiptNumber);
            LocalDateTime now = LocalDateTime.now();

            notificationService.createNotification(
                Notification.NotificationType.PAYMENT_RECEIVED,
                appointment.getPatient().getEmail(),
                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
                appointment,
                "Pago confirmado — Cita médica",
                message,
                Notification.NotificationChannel.EMAIL,
                now
            );

            notificationService.createNotification(
                Notification.NotificationType.PAYMENT_RECEIVED,
                appointment.getPatient().getEmail(),
                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
                appointment,
                "Pago confirmado — Cita médica",
                message,
                Notification.NotificationChannel.IN_APP,
                now
            );
        } catch (Exception e) {
            logger.error("Error notificando pago confirmado para cita ID={}: {}",
                appointment.getId(), e.getMessage());
        }
    }

    // ========================================================
    // MAPEO Entity → DTO
    // ========================================================

    /**
     * Mapea Payment a PaymentResponse.
     * NUNCA devuelve la entidad Payment directamente.
     */
    private PaymentResponse mapToResponse(Payment payment) {
        PatientInsurance policy = payment.getPatientInsurance();

        return new PaymentResponse(
            payment.getId(),
            payment.getBaseAmount(),
            payment.getDeductibleApplied(),
            payment.getInsuranceCoveredAmount(),
            payment.getAmount(),
            policy != null ? policy.getId() : null,
            policy != null ? policy.getInsurance().getName() : null,
            payment.getStatus().name(),
            payment.getMethod() != null ? payment.getMethod().name() : null,
            payment.getDescription(),
            payment.getPaymentDate(),
            payment.getCreatedAt(),
            payment.getUpdatedAt(),
            payment.getAppointment() != null
                    ? payment.getAppointment().getId()
                    : null
        );
    }
}
