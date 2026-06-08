package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.AuditLogResponse;
import com.aviva.appointmentsystem.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Auditoría — solo lectura.
 *
 * RN-42/RN-43: Los logs de auditoría se generan automáticamente en los servicios
 * cuando ocurren acciones clave (CREATED, RESCHEDULED, CANCELLED, PAYMENT_CONFIRMED).
 * Este controlador SOLO expone endpoints GET. No existen endpoints de escritura.
 *
 * Endpoints:
 * - GET /api/audit-logs/appointment/{appointmentId}  → Historial de una cita (orden DESC)
 * - GET /api/audit-logs/{id}                         → Registro individual
 */
@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Auditoría", description = "Trazabilidad inmutable de cambios de estado (RN-42, RN-43) — solo lectura")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Obtiene el historial de auditoría de una cita.
     * GET /api/audit-logs/appointment/{appointmentId}
     *
     * Devuelve todos los eventos registrados para esa cita, ordenados
     * por fecha de creación descendente (el más reciente primero).
     *
     * Eventos típicos: CREATED, RESCHEDULED, CANCELLED, PAYMENT_CONFIRMED.
     */
    @Operation(
        summary = "Historial de auditoría de una cita",
        description = "Devuelve todos los eventos de auditoría registrados para una cita, " +
                      "ordenados por fecha descendente. " +
                      "RN-42/43: Registros inmutables generados automáticamente por el sistema."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Historial obtenido. Lista vacía si no hay eventos registrados aún."
        )
    })
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAppointmentHistory(
            @Parameter(description = "ID de la cita", required = true, example = "1")
            @PathVariable Long appointmentId) {

        logger.info("GET /api/audit-logs/appointment/{}", appointmentId);

        List<AuditLogResponse> response = auditService.getAppointmentHistory(appointmentId);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Historial de auditoría: " + response.size() + " evento(s)")
        );
    }

    /**
     * Obtiene un registro de auditoría individual por ID.
     * GET /api/audit-logs/{id}
     */
    @Operation(
        summary = "Obtener registro de auditoría por ID",
        description = "Devuelve un evento de auditoría específico"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Registro obtenido"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Registro no encontrado"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getById(
            @Parameter(description = "ID del registro de auditoría", required = true, example = "1")
            @PathVariable Long id) {

        logger.info("GET /api/audit-logs/{}", id);

        AuditLogResponse response = auditService.getById(id);

        return ResponseEntity.ok(ApiResponse.success(response, "Registro de auditoría obtenido"));
    }
}
