package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.AuditLogResponse;
import com.aviva.appointmentsystem.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de auditoría
 * Maneja endpoints para consultar historial de cambios
 */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    @Autowired
    private AuditService auditService;

    /**
     * Obtiene el historial de cambios de una cita
     * GET /api/audit-logs/appointment/{appointmentId}
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAppointmentHistory(
            @PathVariable Long appointmentId) {
        logger.info("GET /api/audit-logs/appointment/{} - Obtener historial de auditoría", appointmentId);

        List<AuditLogResponse> response = auditService.getAppointmentHistory(appointmentId);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Historial de auditoría obtenido: " + response.size()));
    }

    /**
     * Obtiene un registro de auditoría por ID
     * GET /api/audit-logs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getById(@PathVariable Long id) {
        logger.info("GET /api/audit-logs/{} - Obtener registro de auditoría", id);

        AuditLogResponse response = auditService.getById(id);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Registro de auditoría obtenido"));
    }
}
