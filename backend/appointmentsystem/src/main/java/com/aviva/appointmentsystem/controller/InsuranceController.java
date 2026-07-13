package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.InsuranceRequest;
import com.aviva.appointmentsystem.dto.InsuranceResponse;
import com.aviva.appointmentsystem.service.InsuranceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de seguros médicos
 * Gestiona aseguradoras y convenios (RN-04 a RN-08)
 */
@RestController
@RequestMapping("/api/insurances")
@Tag(name = "Seguros", description = "Gestion de aseguradoras y condiciones de cobertura")
@SecurityRequirement(name = "bearerAuth")
public class InsuranceController {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceController.class);

    @Autowired
    private InsuranceService insuranceService;

    /**
     * Crea un nuevo seguro
     * POST /api/insurances
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InsuranceResponse>> create(
            @Valid @RequestBody InsuranceRequest request) {
        logger.info("POST /api/insurances - Crear seguro");

        InsuranceResponse response = insuranceService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Seguro creado exitosamente"));
    }

    /**
     * Obtiene todos los seguros activos
     * GET /api/insurances
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<InsuranceResponse>>> getAll() {
        logger.info("GET /api/insurances - Listar seguros");

        List<InsuranceResponse> response = insuranceService.getAll();

        return ResponseEntity
                .ok(ApiResponse.success(response, "Seguros obtenidos: " + response.size()));
    }

    /**
     * Obtiene un seguro por ID
     * GET /api/insurances/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InsuranceResponse>> getById(@PathVariable Long id) {
        logger.info("GET /api/insurances/{} - Obtener seguro", id);

        InsuranceResponse response = insuranceService.getById(id);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Seguro obtenido"));
    }

    /**
     * Actualiza un seguro
     * PUT /api/insurances/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InsuranceResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody InsuranceRequest request) {
        logger.info("PUT /api/insurances/{} - Actualizar seguro", id);

        InsuranceResponse response = insuranceService.update(id, request);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Seguro actualizado exitosamente"));
    }

    /**
     * Desactiva un seguro
     * DELETE /api/insurances/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivate(@PathVariable Long id) {
        logger.info("DELETE /api/insurances/{} - Desactivar seguro", id);

        insuranceService.deactivate(id);

        return ResponseEntity
                .ok(ApiResponse.success("Seguro desactivado exitosamente"));
    }
}
