package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.SpecialtyRequest;
import com.aviva.appointmentsystem.dto.SpecialtyResponse;
import com.aviva.appointmentsystem.service.SpecialtyService;
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
 * Controlador de especialidades médicas
 * Maneja endpoints para CRUD de especialidades
 */
@RestController
@RequestMapping("/api/specialties")
public class SpecialtyController {

    private static final Logger logger = LoggerFactory.getLogger(SpecialtyController.class);

    @Autowired
    private SpecialtyService specialtyService;

    /**
     * Crea una nueva especialidad
     * POST /api/specialties
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SpecialtyResponse>> create(
            @Valid @RequestBody SpecialtyRequest request) {
        logger.info("POST /api/specialties - Crear especialidad");

        SpecialtyResponse response = specialtyService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Especialidad creada exitosamente"));
    }

    /**
     * Actualiza una especialidad
     * PUT /api/specialties/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecialtyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SpecialtyRequest request) {
        logger.info("PUT /api/specialties/{} - Actualizar especialidad", id);

        SpecialtyResponse response = specialtyService.update(id, request);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Especialidad actualizada exitosamente"));
    }

    /**
     * Obtiene una especialidad por ID
     * GET /api/specialties/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecialtyResponse>> getById(@PathVariable Long id) {
        logger.info("GET /api/specialties/{} - Obtener especialidad", id);

        SpecialtyResponse response = specialtyService.getById(id);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Especialidad obtenida"));
    }

    /**
     * Lista todas las especialidades
     * GET /api/specialties
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SpecialtyResponse>>> getAll() {
        logger.info("GET /api/specialties - Listar especialidades");

        List<SpecialtyResponse> response = specialtyService.getAll();

        return ResponseEntity
                .ok(ApiResponse.success(response, "Especialidades obtenidas: " + response.size()));
    }

    /**
     * Desactiva una especialidad
     * DELETE /api/specialties/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivate(@PathVariable Long id) {
        logger.info("DELETE /api/specialties/{} - Desactivar especialidad", id);

        specialtyService.deactivate(id);

        return ResponseEntity
                .ok(ApiResponse.success("Especialidad desactivada exitosamente"));
    }
}
