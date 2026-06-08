package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.ConsultationRequest;
import com.aviva.appointmentsystem.dto.ConsultationResponse;
import com.aviva.appointmentsystem.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Consultas Médicas (diagnóstico + tratamiento).
 *
 * Endpoints:
 * - POST /api/consultations/{appointmentId}  → Registrar consulta (RN-31, RN-33)
 * - GET  /api/consultations/{appointmentId}  → Obtener consulta de una cita
 *
 * RN-31: Solo citas CONFIRMED pueden tener consulta.
 * RN-33: Solo se permite una consulta por cita (1:1).
 */
@RestController
@RequestMapping("/api/consultations")
@Tag(name = "Consultas Médicas", description = "Registro de diagnóstico y tratamiento post-atención (RN-31, RN-33)")
@SecurityRequirement(name = "bearerAuth")
public class ConsultationController {

    private static final Logger logger = LoggerFactory.getLogger(ConsultationController.class);

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    /**
     * Registra la consulta médica (diagnóstico y tratamiento) de una cita.
     * POST /api/consultations/{appointmentId}
     *
     * RN-31: La cita DEBE estar en estado CONFIRMED. Si no, devuelve 409.
     * RN-33: No puede existir consulta previa para la misma cita. Si existe, devuelve 409.
     */
    @Operation(
        summary = "Registrar consulta",
        description = "Registra el diagnóstico y tratamiento de una cita médica. " +
                      "RN-31: La cita debe estar en estado CONFIRMED (pago procesado). " +
                      "RN-33: Solo se permite una consulta por cita."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Consulta registrada exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos inválidos (diagnóstico/tratamiento vacíos o muy cortos)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Cita no encontrada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "RN-31: Cita no está CONFIRMED, o RN-33: Ya existe consulta para esta cita"
        )
    })
    @PostMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<ConsultationResponse>> create(
            @Parameter(description = "ID de la cita (debe estar CONFIRMED)", required = true, example = "1")
            @PathVariable Long appointmentId,
            @Valid @RequestBody ConsultationRequest request) {

        logger.info("POST /api/consultations/{} - Registrar consulta", appointmentId);

        ConsultationResponse response = consultationService.create(appointmentId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Consulta registrada exitosamente"));
    }

    /**
     * Obtiene la consulta médica de una cita.
     * GET /api/consultations/{appointmentId}
     */
    @Operation(
        summary = "Obtener consulta de cita",
        description = "Devuelve el diagnóstico y tratamiento registrado para una cita"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Consulta obtenida exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Cita o consulta no encontrada"
        )
    })
    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<ConsultationResponse>> getByAppointment(
            @Parameter(description = "ID de la cita", required = true, example = "1")
            @PathVariable Long appointmentId) {

        logger.info("GET /api/consultations/{}", appointmentId);

        ConsultationResponse response = consultationService.getByAppointment(appointmentId);

        return ResponseEntity.ok(ApiResponse.success(response, "Consulta obtenida"));
    }
}
