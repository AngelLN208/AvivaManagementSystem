package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.PaymentResponse;
import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.entity.Notification;
import com.aviva.appointmentsystem.entity.Payment;
import com.aviva.appointmentsystem.entity.PaymentMethod;
import com.aviva.appointmentsystem.entity.PaymentStatus;
import com.aviva.appointmentsystem.entity.Receipt;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.ValidationException;
import com.aviva.appointmentsystem.repository.AppointmentRepository;
import com.aviva.appointmentsystem.repository.PaymentRepository;
import com.aviva.appointmentsystem.repository.ReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar pagos
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Procesa un pago y genera el comprobante
     * Cambia el estado del pago a PAGADO y la cita a CONFIRMADA
     */
    public PaymentResponse processPayment(Long appointmentId, PaymentMethod method) {
        logger.info("Procesando pago para cita ID={}", appointmentId);

        // Obtener la cita
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", appointmentId));

        // Obtener el pago
        List<Payment> payments = paymentRepository.findByAppointmentId(appointmentId);
        if (payments.isEmpty()) {
            throw new ResourceNotFoundException("Pago no encontrado para cita: " + appointmentId);
        }

        Payment payment = payments.get(0);

        // Validar que no esté ya pagado
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new ValidationException("El pago ya ha sido procesado");
        }

        // Actualizar pago
        payment.setStatus(PaymentStatus.PAID);
        payment.setMethod(method);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Pago procesado: ID={}", savedPayment.getId());

        // Actualizar cita a CONFIRMADA
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
        logger.info("Cita confirmada: ID={}", appointmentId);

        // Crear notificación de pago recibido
        createPaymentNotification(appointment);

        // Generar comprobante
        generateReceipt(savedPayment);

        return mapToResponse(savedPayment);
    }

    /**
     * Obtiene un pago por ID
     */
    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        logger.debug("Obteniendo pago ID={}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", id));

        return mapToResponse(payment);
    }

    /**
     * Obtiene pagos de una cita
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
     * Lista todos los pagos
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        logger.debug("Obteniendo todos los pagos");

        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lista pagos por estado
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByStatus(PaymentStatus status) {
        logger.debug("Obteniendo pagos con estado: {}", status);

        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Genera un comprobante de pago
     */
    private void generateReceipt(Payment payment) {
        logger.info("Generando comprobante para pago ID={}", payment.getId());

        Receipt receipt = new Receipt();
        receipt.setPayment(payment);
        receipt.setReceiptNumber(generateReceiptNumber());
        receipt.setDescription("Comprobante de pago por consulta médica");
        receipt.setTotalAmount(payment.getAmount());
        receipt.setCreatedAt(LocalDateTime.now());

        receiptRepository.save(receipt);
        logger.info("Comprobante generado: {}", receipt.getReceiptNumber());
    }

    /**
     * Crea notificación de pago recibido
     */
    private void createPaymentNotification(Appointment appointment) {
        try {
            String message = String.format("Tu pago para la cita del %s ha sido recibido. Gracias.",
                appointment.getAppointmentDateTime().toLocalDate());

            notificationService.createNotification(
                Notification.NotificationType.PAYMENT_RECEIVED,
                appointment.getPatient().getEmail(),
                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
                appointment,
                "Pago recibido",
                message,
                Notification.NotificationChannel.EMAIL,
                LocalDateTime.now()
            );

            logger.info("Notificación de pago creada para cita: ID={}", appointment.getId());
        } catch (Exception e) {
            logger.error("Error creando notificación de pago para cita: ID={}", appointment.getId(), e);
        }
    }

    /**
     * Genera un número de comprobante único
     */
    private String generateReceiptNumber() {
        return "RCP-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Mapea entidad a DTO
     */
    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getAmount(),
            payment.getStatus().name(),
            payment.getMethod().name(),
            payment.getDescription(),
            payment.getPaymentDate(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }
}
