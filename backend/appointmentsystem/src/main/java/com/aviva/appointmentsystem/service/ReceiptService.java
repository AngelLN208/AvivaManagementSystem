package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.ReceiptResponse;
import com.aviva.appointmentsystem.entity.Receipt;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
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

    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
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
        logger.debug("Obteniendo comprobante ID={}", id);
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
        logger.debug("Obteniendo comprobante por número: {}", receiptNumber);
        Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Comprobante no encontrado con número: " + receiptNumber));
        return mapToResponse(receipt);
    }

    // ========================================================
    // MAPEO Entity → DTO
    // ========================================================

    /**
     * Mapea Receipt a ReceiptResponse enriquecido.
     * Incluye paymentId y appointmentId para navegación del frontend.
     */
    private ReceiptResponse mapToResponse(Receipt receipt) {
        Long appointmentId = receipt.getPayment() != null && receipt.getPayment().getAppointment() != null
            ? receipt.getPayment().getAppointment().getId()
            : null;

        return new ReceiptResponse(
            receipt.getId(),
            receipt.getReceiptNumber(),
            receipt.getDescription(),
            receipt.getTotalAmount(),
            receipt.getPayment() != null ? receipt.getPayment().getId() : null,
            appointmentId,
            receipt.getCreatedAt()
        );
    }
}
