package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.ReceiptResponse;
import com.aviva.appointmentsystem.service.ReceiptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de comprobantes
 * Maneja endpoints para consultar comprobantes de pago
 */
@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptController.class);

    @Autowired
    private ReceiptService receiptService;

    /**
     * Obtiene un comprobante por ID
     * GET /api/receipts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getById(@PathVariable Long id) {
        logger.info("GET /api/receipts/{} - Obtener comprobante", id);

        ReceiptResponse response = receiptService.getById(id);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Comprobante obtenido"));
    }

    /**
     * Obtiene un comprobante por número
     * GET /api/receipts/number/{receiptNumber}
     */
    @GetMapping("/number/{receiptNumber}")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getByReceiptNumber(
            @PathVariable String receiptNumber) {
        logger.info("GET /api/receipts/number/{} - Obtener comprobante por número", receiptNumber);

        ReceiptResponse response = receiptService.getByReceiptNumber(receiptNumber);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Comprobante obtenido"));
    }

    /**
     * Lista todos los comprobantes
     * GET /api/receipts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReceiptResponse>>> getAll() {
        logger.info("GET /api/receipts - Listar comprobantes");

        List<ReceiptResponse> response = receiptService.getAll();

        return ResponseEntity
                .ok(ApiResponse.success(response, "Comprobantes obtenidos: " + response.size()));
    }
}
