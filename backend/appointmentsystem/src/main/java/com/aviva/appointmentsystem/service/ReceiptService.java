package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.dto.ReceiptResponse;
import com.aviva.appointmentsystem.entity.Receipt;
import com.aviva.appointmentsystem.exception.ResourceNotFoundException;
import com.aviva.appointmentsystem.repository.ReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestionar comprobantes de pago
 */
@Service
@Transactional
public class ReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptService.class);

    @Autowired
    private ReceiptRepository receiptRepository;

    /**
     * Obtiene un comprobante por ID
     */
    @Transactional(readOnly = true)
    public ReceiptResponse getById(Long id) {
        logger.debug("Obteniendo comprobante ID={}", id);

        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante", id));

        return mapToResponse(receipt);
    }

    /**
     * Obtiene un comprobante por número de comprobante
     */
    @Transactional(readOnly = true)
    public ReceiptResponse getByReceiptNumber(String receiptNumber) {
        logger.debug("Obteniendo comprobante por número: {}", receiptNumber);

        Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado: " + receiptNumber));

        return mapToResponse(receipt);
    }

    /**
     * Lista todos los comprobantes
     */
    @Transactional(readOnly = true)
    public List<ReceiptResponse> getAll() {
        logger.debug("Obteniendo todos los comprobantes");

        return receiptRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Mapea entidad a DTO
     */
    private ReceiptResponse mapToResponse(Receipt receipt) {
        return new ReceiptResponse(
            receipt.getId(),
            receipt.getReceiptNumber(),
            receipt.getDescription(),
            receipt.getTotalAmount(),
            receipt.getCreatedAt()
        );
    }
}
