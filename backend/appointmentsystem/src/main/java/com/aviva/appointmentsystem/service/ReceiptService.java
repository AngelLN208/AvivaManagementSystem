package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.ReceiptResponse;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.Receipt;
import com.aviva.appointmentsystem.entity.UserStatus;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.exception.UserInactiveException;
import com.aviva.appointmentsystem.repository.PatientRepository;
import com.aviva.appointmentsystem.repository.ReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de solo lectura para comprobantes de pago (Receipts).
 *
 * Los comprobantes son INMUTABLES — se generan automáticamente en PaymentService (RN-28)
 * y NUNCA se crean, modifican o eliminan vía API.
 *
 * Endpoints disponibles (solo GET):
 * - GET /api/receipts               → Listar todos
 * - GET /api/receipts/{id}          → Por ID
 * - GET /api/receipts/number/{num}  → Por número de comprobante
 *
 * Inyección: Constructor injection (no @Autowired).
 */
@Service
@Transactional(readOnly = true)
public class ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);

    private final ReceiptRepository receiptRepository;
    private final PatientRepository patientRepository;
    private final ReceiptPdfService receiptPdfService;

    public ReceiptService(
            ReceiptRepository receiptRepository,
            PatientRepository patientRepository,
            ReceiptPdfService receiptPdfService) {
        this.receiptRepository = receiptRepository;
        this.patientRepository = patientRepository;
        this.receiptPdfService = receiptPdfService;
    }

    /**
     * Lista todos los comprobantes del sistema.
     */
    public List<ReceiptResponse> getAll() {
        logger.debug("Obteniendo todos los comprobantes");
        return receiptRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene un comprobante por ID.
     *
     * @param id ID del comprobante
     * @throws ResourceNotFoundException si no existe
     */
    public ReceiptResponse getById(Long id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante", id));
        return mapToResponse(receipt);
    }

    /**
     * Obtiene un comprobante por su número único (ej: RCP-20260520-A3F7B2C1).
     *
     * @param receiptNumber número de comprobante
     * @throws ResourceNotFoundException si no existe
     */
    public ReceiptResponse getByReceiptNumber(String receiptNumber) {
        Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comprobante no encontrado con número: " + receiptNumber));
        return mapToResponse(receipt);
    }

    /** Lista solamente las constancias del paciente asociado al JWT. */
    public List<ReceiptResponse> getForCurrentPatient(String username) {
        Patient patient = requirePatientProfile(username);
        return receiptRepository
                .findByPaymentAppointmentPatientIdOrderByCreatedAtDesc(patient.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /** Devuelve 404 tanto para una constancia inexistente como para una ajena. */
    public ReceiptResponse getByIdForCurrentPatient(
            String username,
            Long receiptId) {

        return mapToResponse(requireOwnedReceipt(username, receiptId));
    }

    /**
     * Genera la descarga PDF después de validar que la constancia pertenece al
     * paciente autenticado. El PDF no se persiste: se construye desde el recibo
     * inmutable y se entrega directamente al controlador.
     */
    public ReceiptPdfDocument generatePdfForCurrentPatient(
            String username,
            Long receiptId) {
        return receiptPdfService.generate(requireOwnedReceipt(username, receiptId));
    }

    /** Obtiene la constancia de un pago únicamente si ambos son del paciente. */
    public ReceiptResponse getByPaymentForCurrentPatient(
            String username,
            Long paymentId) {

        Patient patient = requirePatientProfile(username);
        Receipt receipt = receiptRepository
                .findByPaymentIdAndPaymentAppointmentPatientId(
                        paymentId, patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comprobante del pago", paymentId));
        return mapToResponse(receipt);
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

    private Receipt requireOwnedReceipt(String username, Long receiptId) {
        Patient patient = requirePatientProfile(username);
        return receiptRepository
                .findByIdAndPaymentAppointmentPatientId(receiptId, patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comprobante", receiptId));
    }

    // ========================================================
    // MAPEO Entity → DTO
    // ========================================================

    /**
     * Mapea Receipt a ReceiptResponse enriquecido.
     * Incluye paymentId y appointmentId para navegación del frontend.
     */
    private ReceiptResponse mapToResponse(Receipt receipt) {
        Long paymentId = receipt.getPayment() != null
                ? receipt.getPayment().getId()
                : null;
        Long appointmentId = receipt.getPayment() != null
                && receipt.getPayment().getAppointment() != null
                ? receipt.getPayment().getAppointment().getId()
                : null;
        return new ReceiptResponse(
                receipt.getId(),
                receipt.getReceiptNumber(),
                receipt.getDescription(),
                receipt.getTotalAmount(),
                paymentId,
                appointmentId,
                receipt.getCreatedAt()
        );
    }
}
