package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.ReceiptResponse;
import com.aviva.appointmentsystem.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de Comprobantes (Receipts) — solo lectura.
 *
 * Los comprobantes son generados automáticamente por PaymentService (RN-28)
 * cuando se procesa un pago. Este controlador SOLO expone endpoints GET.
 *
 * Endpoints:
 * - GET /api/receipts                         → Listar todos
 * - GET /api/receipts/{id}                    → Por ID
 * - GET /api/receipts/number/{receiptNumber}  → Por número de comprobante
 */
@RestController
@RequestMapping("/api/receipts")
@Tag(name = "Comprobantes", description = "Consulta de comprobantes de pago (solo lectura — generados automáticamente por RN-28)")
@SecurityRequirement(name = "bearerAuth")
public class ReceiptController {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptController.class);

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    /**
     * Lista todos los comprobantes del sistema.
     * GET /api/receipts
     */
    @Operation(
        summary = "Listar comprobantes",
        description = "Devuelve todos los comprobantes de pago generados. Los comprobantes son inmutables."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Lista de comprobantes obtenida"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReceiptResponse>>> getAll() {
        logger.info("GET /api/receipts - Listar comprobantes");
        List<ReceiptResponse> response = receiptService.getAll();
        return ResponseEntity.ok(
            ApiResponse.success(response, "Comprobantes obtenidos: " + response.size()));
    }

    /**
     * Obtiene un comprobante por ID.
     * GET /api/receipts/{id}
     */
    @Operation(
        summary = "Obtener comprobante por ID",
        description = "Devuelve los detalles de un comprobante incluyendo paymentId y appointmentId asociados"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Comprobante encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Comprobante no encontrado"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getById(
            @Parameter(description = "ID del comprobante", required = true, example = "1")
            @PathVariable Long id) {

        logger.info("GET /api/receipts/{}", id);
        ReceiptResponse response = receiptService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Comprobante obtenido"));
    }

    /**
     * Obtiene un comprobante por número.
     * GET /api/receipts/number/{receiptNumber}
     *
     * Formato del número: RCP-{yyyyMMdd}-{8chars}
     * Ejemplo: RCP-20260520-A3F7B2C1
     */
    @Operation(
        summary = "Obtener comprobante por número",
        description = "Busca un comprobante por su número único. " +
                      "Formato: RCP-{yyyyMMdd}-{8chars}. Ejemplo: RCP-20260520-A3F7B2C1"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Comprobante encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Comprobante no encontrado con ese número"
        )
    })
    @GetMapping("/number/{receiptNumber}")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getByReceiptNumber(
            @Parameter(
                description = "Número de comprobante",
                required = true,
                example = "RCP-20260520-A3F7B2C1"
            )
            @PathVariable String receiptNumber) {

        logger.info("GET /api/receipts/number/{}", receiptNumber);
        ReceiptResponse response = receiptService.getByReceiptNumber(receiptNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "Comprobante obtenido"));
    }
}
