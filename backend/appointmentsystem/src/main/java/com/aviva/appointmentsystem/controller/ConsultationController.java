package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.ConsultationRequest;
import com.aviva.appointmentsystem.dto.ConsultationResponse;
import com.aviva.appointmentsystem.service.ConsultationService;
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
 * Controlador de consultas (diagnósticos)
 * Maneja endpoints para registrar y consultar diagnósticos de citas
 */
@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private static final Logger logger = LoggerFactory.getLogger(ConsultationController.class);

    @Autowired
    private ConsultationService consultationService;

    /**
     * Registra una consulta (diagnóstico y tratamiento)
     * POST /api/consultations/{appointmentId}
     */
    @PostMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<ConsultationResponse>> create(
            @PathVariable Long appointmentId,
            @Valid @RequestBody ConsultationRequest request) {
        logger.info("POST /api/consultations/{} - Registrar consulta", appointmentId);

        ConsultationResponse response = consultationService.create(appointmentId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Consulta registrada exitosamente"));
    }

    /**
     * Obtiene la consulta de una cita
     * GET /api/consultations/{appointmentId}
     */
    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<ConsultationResponse>> getByAppointment(
            @PathVariable Long appointmentId) {
        logger.info("GET /api/consultations/{} - Obtener consulta", appointmentId);

        ConsultationResponse response = consultationService.getByAppointment(appointmentId);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Consulta obtenida"));
    }
}
