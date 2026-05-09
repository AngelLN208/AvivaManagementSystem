package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.DoctorRequest;
import com.aviva.appointmentsystem.dto.DoctorResponse;
import com.aviva.appointmentsystem.service.DoctorService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de doctores
 * Maneja endpoints para CRUD de doctores
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);

    @Autowired
    private DoctorService doctorService;

    /**
     * Crea un nuevo doctor
     * POST /api/doctors
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DoctorResponse>> create(
            @Valid @RequestBody DoctorRequest request) {
        logger.info("POST /api/doctors - Crear doctor");

        DoctorResponse response = doctorService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Doctor creado exitosamente"));
    }

    /**
     * Actualiza un doctor
     * PUT /api/doctors/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {
        logger.info("PUT /api/doctors/{} - Actualizar doctor", id);

        DoctorResponse response = doctorService.update(id, request);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Doctor actualizado exitosamente"));
    }

    /**
     * Obtiene un doctor por ID
     * GET /api/doctors/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> getById(@PathVariable Long id) {
        logger.info("GET /api/doctors/{} - Obtener doctor", id);

        DoctorResponse response = doctorService.getById(id);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Doctor obtenido"));
    }

    /**
     * Lista todos los doctores
     * GET /api/doctors
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAll() {
        logger.info("GET /api/doctors - Listar doctores");

        List<DoctorResponse> response = doctorService.getAll();

        return ResponseEntity
                .ok(ApiResponse.success(response, "Doctores obtenidos: " + response.size()));
    }

    /**
     * Obtiene doctores por especialidad
     * GET /api/doctors/by-specialty/{specialtyId}
     */
    @GetMapping("/by-specialty/{specialtyId}")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getBySpecialty(
            @PathVariable Long specialtyId) {
        logger.info("GET /api/doctors/by-specialty/{} - Obtener doctores por especialidad", specialtyId);

        List<DoctorResponse> response = doctorService.getBySpecialty(specialtyId);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Doctores encontrados: " + response.size()));
    }

    /**
     * Desactiva un doctor
     * DELETE /api/doctors/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivate(@PathVariable Long id) {
        logger.info("DELETE /api/doctors/{} - Desactivar doctor", id);

        doctorService.deactivate(id);

        return ResponseEntity
                .ok(ApiResponse.success("Doctor desactivado exitosamente"));
    }
}
