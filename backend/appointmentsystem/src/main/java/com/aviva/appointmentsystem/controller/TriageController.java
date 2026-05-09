package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.TriageRequest;
import com.aviva.appointmentsystem.dto.TriageResponse;
import com.aviva.appointmentsystem.service.TriageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de triaje (signos vitales)
 * Maneja endpoints para registrar y consultar triaje de citas
 */
@RestController
@RequestMapping("/api/triages")
public class TriageController {

    private static final Logger logger = LoggerFactory.getLogger(TriageController.class);

    @Autowired
    private TriageService triageService;

    /**
     * Registra los signos vitales de una cita
     * POST /api/triages/{appointmentId}
     */
    @PostMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<TriageResponse>> create(
            @PathVariable Long appointmentId,
            @Valid @RequestBody TriageRequest request) {
        logger.info("POST /api/triages/{} - Registrar triaje", appointmentId);

        TriageResponse response = triageService.create(appointmentId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Triaje registrado exitosamente"));
    }

    /**
     * Obtiene el triaje de una cita
     * GET /api/triages/{appointmentId}
     */
    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<TriageResponse>> getByAppointment(
            @PathVariable Long appointmentId) {
        logger.info("GET /api/triages/{} - Obtener triaje", appointmentId);

        TriageResponse response = triageService.getByAppointment(appointmentId);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Triaje obtenido"));
    }
}
