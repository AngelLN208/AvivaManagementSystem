package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.PaymentResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.entity.AuditLog;
import com.aviva.appointmentsystem.entity.Notification;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.PatientInsurance;
import com.aviva.appointmentsystem.entity.Payment;
import com.aviva.appointmentsystem.entity.PaymentMethod;
import com.aviva.appointmentsystem.entity.PaymentStatus;
import com.aviva.appointmentsystem.entity.Receipt;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.BusinessRuleException;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.UserInactiveException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.AuditLogRepository;
import com.aviva.appointmentsystem.repository.PatientInsuranceRepository;
import com.aviva.appointmentsystem.repository.PatientRepository;
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
 * Mantiene dos entradas con contratos independientes:
 *
 * - processPayment conserva el flujo administrativo histórico de staff.
 * - payForCurrentPatient valida identidad, ownership y reglas del portal.
 *
 * Ambas entradas convergen en una única cadena transaccional:
 * Payment(PENDING→PAID) → Appointment(CONFIRMED) → AuditLog → Receipt →
 * Notification. La cobertura del seguro se vuelve a validar antes del commit.
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
    private final PatientRepository patientRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReceiptRepository receiptRepository,
            AppointmentRepository appointmentRepository,
            AuditLogRepository auditLogRepository,
            NotificationService notificationService,
            PatientInsuranceRepository patientInsuranceRepository,
            PatientRepository patientRepository) {

        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
        this.patientInsuranceRepository = patientInsuranceRepository;
        this.patientRepository = patientRepository;
    }

    // ========================================================
    // OPERACIÓN PRINCIPAL
    // ========================================================

    /**
     * Procesa un pago por su ID.
     *
     * Entrada histórica usada por administración y recepción. Recibe el ID del
     * Payment, no el ID de la Appointment, y conserva los métodos admitidos por
     * caja. El lock evita dos confirmaciones concurrentes del mismo pago.
     *
     * @param paymentId ID del pago a procesar
     * @param method    método de pago (CASH, CREDIT_CARD, DEBIT_CARD, TRANSFER, INSURANCE)
     * @return datos del pago procesado
     */
    public PaymentResponse processPayment(Long paymentId, PaymentMethod method) {
        logger.info("Procesando pago ID={} con método={}", paymentId, method);

        // 1. Obtener el pago
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", paymentId));

        validatePaymentState(payment);
        validatePaymentMethod(method);
        return completePayment(payment, method, "SYSTEM");
    }

    /**
     * Registra un pago desde el portal únicamente si el recurso pertenece al
     * paciente del JWT. Para saldos positivos admite crédito o débito; una
     * cobertura total válida se resuelve en el servidor como INSURANCE.
     */
    public PaymentResponse payForCurrentPatient(
            String username,
            Long paymentId,
            PaymentMethod requestedMethod) {

        Patient patient = requirePatientProfile(username);
        Payment payment = paymentRepository.findOwnedByIdForUpdate(
                        paymentId, patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pago", paymentId));

        validatePaymentState(payment);
        validatePatientAppointment(payment.getAppointment(), LocalDateTime.now());
        PaymentMethod method = resolvePatientPaymentMethod(payment, requestedMethod);
        return completePayment(payment, method, username);
    }

    /** Ejecuta los efectos comunes dentro de la misma transacción. */
    private PaymentResponse completePayment(
            Payment payment,
            PaymentMethod method,
            String actor) {

        Long paymentId = payment.getId();
        LocalDateTime now = LocalDateTime.now();

        Appointment appointment = payment.getAppointment();

        payment.setStatus(PaymentStatus.PAID);
        payment.setMethod(method);
        payment.setPaymentDate(now);
        payment.setUpdatedAt(now);
        Payment savedPayment = paymentRepository.save(payment);

        updateAnnualInsuranceCoverage(savedPayment, now);
        logger.info("Pago ID={} marcado como PAID", paymentId);

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(now);
        appointmentRepository.save(appointment);

        registerAudit(
                appointment,
                "PAYMENT_CONFIRMED",
                String.format(
                        "Pago ID=%d procesado con método %s. Cita confirmada.",
                        paymentId, method),
                actor);

        Receipt receipt = generateReceipt(savedPayment, now);
        logger.info("Constancia generada: {}", receipt.getReceiptNumber());

        notifyPaymentConfirmed(appointment, receipt.getReceiptNumber());

        return mapToResponse(savedPayment);
    }

    private void validatePaymentState(Payment payment) {
        if (payment.getStatus() == PaymentStatus.PENDING) {
            return;
        }

        String code = switch (payment.getStatus()) {
            case PAID -> "RN-26_ALREADY_PAID";
            case CANCELLED -> "RN-26_CANCELLED";
            case REFUNDED -> "RN-26_REFUNDED";
            default -> "RN-26_INVALID_STATUS";
        };

        throw new BusinessRuleException(
                code,
                "No se puede procesar un pago en estado " + payment.getStatus()
        );
    }

    private void validatePaymentMethod(PaymentMethod method) {
        if (method == null) {
            throw new BusinessRuleException(
                    "PAYMENT_METHOD_REQUIRED",
                    "Debe indicar un método de pago"
            );
        }
    }

    private void validatePatientAppointment(
            Appointment appointment,
            LocalDateTime now) {

        if (appointment == null
                || (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.RESCHEDULED)) {
            throw new BusinessRuleException(
                    "PAYMENT_APPOINTMENT_NOT_PAYABLE",
                    "La cita asociada no se encuentra pendiente de confirmación"
            );
        }

        if (!appointment.getAppointmentDateTime().isAfter(now)) {
            throw new BusinessRuleException(
                    "PAYMENT_APPOINTMENT_EXPIRED",
                    "No se puede procesar el pago de una cita cuya hora ya pasó"
            );
        }
    }

    private PaymentMethod resolvePatientPaymentMethod(
            Payment payment,
            PaymentMethod requestedMethod) {

        BigDecimal amount = payment.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException(
                    "PAYMENT_INVALID_AMOUNT",
                    "El pago tiene un monto inválido"
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal covered = payment.getInsuranceCoveredAmount();
            if (payment.getPatientInsurance() != null
                    && covered != null
                    && covered.compareTo(BigDecimal.ZERO) > 0) {
                return PaymentMethod.INSURANCE;
            }

            throw new BusinessRuleException(
                    "PAYMENT_INVALID_AMOUNT",
                    "El pago sin saldo no tiene cobertura de seguro asociada"
            );
        }

        if (requestedMethod != PaymentMethod.CREDIT_CARD
                && requestedMethod != PaymentMethod.DEBIT_CARD) {
            throw new BusinessRuleException(
                    "PATIENT_PAYMENT_METHOD_NOT_ALLOWED",
                    "El portal solo admite tarjeta de crédito o débito para este pago"
            );
        }

        return requestedMethod;
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

    /** Lista solamente los pagos del paciente asociado al JWT. */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getForCurrentPatient(String username) {
        Patient patient = requirePatientProfile(username);
        return paymentRepository
                .findByAppointmentPatientIdOrderByCreatedAtDesc(patient.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /** Devuelve 404 tanto para un pago inexistente como para uno ajeno. */
    @Transactional(readOnly = true)
    public PaymentResponse getByIdForCurrentPatient(
            String username,
            Long paymentId) {

        Patient patient = requirePatientProfile(username);
        Payment payment = paymentRepository
                .findByIdAndAppointmentPatientId(paymentId, patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pago", paymentId));
        return mapToResponse(payment);
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
    private Receipt generateReceipt(
            Payment payment,
            LocalDateTime now) {

        String receiptNumber = "RCP-"
                + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Receipt receipt = new Receipt();
        receipt.setPayment(payment);
        receipt.setReceiptNumber(receiptNumber);
        receipt.setTotalAmount(payment.getAmount());
        receipt.setDescription(String.format(
                "Comprobante por consulta médica — Cita ID=%d",
                payment.getAppointment().getId()));
        receipt.setCreatedAt(now);

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

        PatientInsurance policyReference = payment.getPatientInsurance();
        BigDecimal coveredAmount = payment.getInsuranceCoveredAmount();

        if (policyReference == null
                || coveredAmount == null
                || coveredAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        PatientInsurance policy = patientInsuranceRepository
                .findByIdForUpdate(policyReference.getId())
                .orElseThrow(() -> new BusinessRuleException(
                        "INSURANCE_POLICY_NOT_AVAILABLE",
                        "La póliza usada para calcular el pago ya no existe"
                ));

        LocalDateTime appointmentDateTime = payment.getAppointment()
                .getAppointmentDateTime();

        if (!Boolean.TRUE.equals(policy.getActive())
                || policy.getInsurance() == null
                || !Boolean.TRUE.equals(policy.getInsurance().getActive())
                || policy.getEffectiveDate() == null
                || policy.getExpirationDate() == null
                || appointmentDateTime.isBefore(policy.getEffectiveDate())
                || appointmentDateTime.isAfter(policy.getExpirationDate())) {
            throw new BusinessRuleException(
                    "INSURANCE_POLICY_NOT_AVAILABLE",
                    "La póliza usada para calcular el pago ya no está vigente"
            );
        }

        BigDecimal currentUsage = policy.getUsedAnnualCoverage() != null
                ? policy.getUsedAnnualCoverage()
                : BigDecimal.ZERO;
        BigDecimal newUsage = currentUsage.add(coveredAmount);
        BigDecimal annualLimit = policy.getInsurance().getMaxAnnualCoverage();

        if (annualLimit != null && newUsage.compareTo(annualLimit) > 0) {
            throw new BusinessRuleException(
                    "INSURANCE_COVERAGE_CHANGED",
                    "La cobertura anual disponible cambió; debe recalcularse el pago"
            );
        }

        policy.setUsedAnnualCoverage(newUsage);
        policy.setUpdatedAt(updateTime);
        patientInsuranceRepository.save(policy);
    }

    /**
     * Registra un evento en el log de auditoría de la cita.
     * Wrapped en try-catch: una falla de auditoría no debe revertir el pago.
     */
    private void registerAudit(
            Appointment appointment,
            String action,
            String details,
            String modifiedBy) {

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
            logger.error(
                    "Error registrando auditoría para cita ID={}: {}",
                    appointment.getId(), e.getMessage());
        }
    }

    /**
     * Notifica al paciente que el pago fue confirmado.
     * Wrapped en try-catch: una falla de notificación no revierte el pago.
     */
    private void notifyPaymentConfirmed(
            Appointment appointment,
            String receiptNumber) {

        try {
            String message = String.format(
                    "Tu pago para la cita del %s ha sido confirmado. "
                            + "Comprobante: %s. Tu cita quedó confirmada.",
                    appointment.getAppointmentDateTime().toLocalDate(),
                    receiptNumber);
            String subject = "Pago confirmado — Cita médica";
            LocalDateTime now = LocalDateTime.now();

            notificationService.createNotification(
                    Notification.NotificationType.PAYMENT_RECEIVED,
                    appointment.getPatient().getEmail(),
                    appointment.getPatient().getFirstName() + " "
                            + appointment.getPatient().getLastName(),
                    appointment,
                    subject,
                    message,
                    Notification.NotificationChannel.EMAIL,
                    now
            );

            notificationService.createNotification(
                    Notification.NotificationType.PAYMENT_RECEIVED,
                    appointment.getPatient().getEmail(),
                    appointment.getPatient().getFirstName() + " "
                            + appointment.getPatient().getLastName(),
                    appointment,
                    subject,
                    message,
                    Notification.NotificationChannel.IN_APP,
                    now
            );
        } catch (Exception e) {
            logger.error(
                    "Error notificando pago confirmado para cita ID={}: {}",
                    appointment.getId(), e.getMessage());
        }
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
