package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.PatientPaymentRequest;
import com.aviva.appointmentsystem.dto.PaymentResponse;
import com.aviva.appointmentsystem.entity.PaymentMethod;
import com.aviva.appointmentsystem.entity.PaymentStatus;
import com.aviva.appointmentsystem.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Controlador de Pagos.
 *
 * Endpoints:
 * - GET  /api/payments                          → Listar todos los pagos
 * - GET  /api/payments/{id}                     → Obtener pago por ID
 * - GET  /api/payments/appointment/{id}         → Pagos de una cita
 * - GET  /api/payments/status/{status}          → Pagos por estado
 * - POST /api/payments/{id}/process?method=...  → Procesar pago (cadena RN-26 → RN-16 → RN-28)
 *
 * IMPORTANTE: El endpoint process() recibe el ID del Payment (no de la Appointment).
 * El cliente debe consultar GET /api/payments/appointment/{appointmentId} primero para obtener el paymentId.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Pagos", description = "Gestión de pagos: procesamiento, consulta y auditoría financiera")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ========================================================
    // PORTAL DEL PACIENTE (/me)
    // ========================================================

    @Operation(
        summary = "Listar mis pagos",
        description = "Devuelve únicamente los pagos del paciente autenticado"
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMine(
            Principal principal) {

        List<PaymentResponse> response =
                paymentService.getForCurrentPatient(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(
                response, "Mis pagos obtenidos: " + response.size()));
    }

    @Operation(
        summary = "Obtener uno de mis pagos",
        description = "Devuelve el pago solo cuando pertenece al paciente autenticado"
    )
    @GetMapping("/me/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getMineById(
            Principal principal,
            @PathVariable Long id) {

        PaymentResponse response = paymentService.getByIdForCurrentPatient(
                principal.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Pago obtenido"));
    }

    @Operation(
        summary = "Registrar el pago de una cita propia",
        description = "Confirma una cita propia y genera su constancia. "
                + "Métodos admitidos para saldos positivos: CREDIT_CARD y DEBIT_CARD."
    )
    @PostMapping("/me/{id}/pay")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<PaymentResponse>> payMine(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody PatientPaymentRequest request) {

        PaymentMethod paymentMethod = parsePaymentMethod(request.method());
        PaymentResponse response = paymentService.payForCurrentPatient(
                principal.getName(), id, paymentMethod);

        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Pago registrado correctamente. Constancia generada."
        ));
    }

    // ========================================================
    // CONSULTAS (GET)
    // ========================================================

    /**
     * Lista todos los pagos del sistema.
     * GET /api/payments
     */
    @Operation(
        summary = "Listar todos los pagos",
        description = "Devuelve todos los pagos registrados en el sistema con su estado actual"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Lista de pagos obtenida"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAll() {
        logger.info("GET /api/payments - Listar pagos");
        List<PaymentResponse> response = paymentService.getAll();
        return ResponseEntity.ok(ApiResponse.success(response, "Pagos obtenidos: " + response.size()));
    }

    /**
     * Obtiene un pago por ID.
     * GET /api/payments/{id}
     */
    @Operation(
        summary = "Obtener pago por ID",
        description = "Devuelve los detalles de un pago incluyendo estado, método y fecha de pago"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Pago encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Pago no encontrado"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(
            @Parameter(description = "ID del pago", required = true, example = "1")
            @PathVariable Long id) {

        logger.info("GET /api/payments/{}", id);
        PaymentResponse response = paymentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Pago obtenido"));
    }

    /**
     * Obtiene los pagos de una cita específica.
     * GET /api/payments/appointment/{appointmentId}
     *
     * Usar este endpoint para obtener el paymentId antes de llamar a /process.
     */
    @Operation(
        summary = "Pagos de una cita",
        description = "Obtiene los pagos asociados a una cita. " +
                      "Usar para obtener el paymentId antes de procesar el pago."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Pagos de la cita obtenidos"
        )
    })
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByAppointment(
            @Parameter(description = "ID de la cita", required = true, example = "1")
            @PathVariable Long appointmentId) {

        logger.info("GET /api/payments/appointment/{}", appointmentId);
        List<PaymentResponse> response = paymentService.getByAppointment(appointmentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Pagos encontrados: " + response.size()));
    }

    /**
     * Lista pagos por estado.
     * GET /api/payments/status/{status}
     */
    @Operation(
        summary = "Pagos por estado",
        description = "Filtra pagos por estado. " +
                      "Estados válidos: PENDING, PAID, CANCELLED, REFUNDED"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Pagos filtrados por estado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Estado inválido"
        )
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByStatus(
            @Parameter(
                description = "Estado del pago",
                required = true,
                example = "PENDING"
            )
            @PathVariable String status) {

        logger.info("GET /api/payments/status/{}", status);
        PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Estado inválido: '" + status + "'. Valores válidos: PENDING, PAID, CANCELLED, REFUNDED");
        }
        List<PaymentResponse> response = paymentService.getByStatus(paymentStatus);
        return ResponseEntity.ok(ApiResponse.success(response, "Pagos con estado " + status + ": " + response.size()));
    }

    // ========================================================
    // OPERACIÓN DE PROCESAMIENTO (POST)
    // ========================================================

    /**
     * Procesa un pago y activa la cadena de estados:
     * Payment(PENDING→PAID) → Appointment(→CONFIRMED) → Receipt(generado)
     *
     * POST /api/payments/{id}/process?method=CASH
     *
     * Donde {id} es el ID del Payment (obtenido de GET /api/payments/appointment/{appointmentId}).
     *
     * RN-26: Payment → PAID
     * RN-16: Appointment → CONFIRMED
     * RN-28: Receipt generado automáticamente
     */
    @Operation(
        summary = "Procesar pago",
        description = "Procesa un pago en estado PENDING. " +
                      "RN-26: Cambia el pago a PAID. " +
                      "RN-16: Confirma la cita asociada (→CONFIRMED). " +
                      "RN-28: Genera un comprobante (Receipt) automáticamente. " +
                      "El parámetro {id} es el ID del Payment, no de la Appointment. " +
                      "Métodos válidos: CASH, CREDIT_CARD, DEBIT_CARD, TRANSFER, INSURANCE"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Pago procesado, cita confirmada y comprobante generado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Método de pago inválido"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Pago no encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "RN-26: Pago ya procesado o cancelado"
        )
    })
    @PostMapping("/{id}/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Parameter(description = "ID del pago (Payment)", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(
                description = "Método de pago",
                required = true,
                example = "CASH"
            )
            @RequestParam String method) {

        logger.info("POST /api/payments/{}/process?method={}", id, method);

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Método de pago inválido: '" + method + "'. " +
                "Valores válidos: CASH, CREDIT_CARD, DEBIT_CARD, TRANSFER, INSURANCE");
        }

        PaymentResponse response = paymentService.processPayment(id, paymentMethod);
        return ResponseEntity.ok(
            ApiResponse.success(response, "Pago procesado exitosamente. Cita confirmada y comprobante generado."));
    }

    private PaymentMethod parsePaymentMethod(String method) {
        try {
            return PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Método de pago inválido: '" + method + "'. "
                + "Valores válidos: CASH, CREDIT_CARD, DEBIT_CARD, TRANSFER, INSURANCE");
        }
    }
}
