package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.PatientRequest;
import com.aviva.appointmentsystem.dto.PatientResponse;
import com.aviva.appointmentsystem.service.PatientService;
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
 * Controlador de pacientes
 * Maneja endpoints para CRUD de pacientes
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    private PatientService patientService;

    /**
     * Crea un nuevo paciente
     * POST /api/patients
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PatientResponse>> create(
            @Valid @RequestBody PatientRequest request) {
        logger.info("POST /api/patients - Crear paciente");

        PatientResponse response = patientService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Paciente creado exitosamente"));
    }

    /**
     * Actualiza un paciente
     * PUT /api/patients/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        logger.info("PUT /api/patients/{} - Actualizar paciente", id);

        PatientResponse response = patientService.update(id, request);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Paciente actualizado exitosamente"));
    }

    /**
     * Obtiene un paciente por ID
     * GET /api/patients/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> getById(@PathVariable Long id) {
        logger.info("GET /api/patients/{} - Obtener paciente", id);

        PatientResponse response = patientService.getById(id);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Paciente obtenido"));
    }

    /**
     * Obtiene un paciente por DNI
     * GET /api/patients/search/dni?dni=...
     */
    @GetMapping("/search/dni")
    public ResponseEntity<ApiResponse<PatientResponse>> getByDni(
            @RequestParam String dni) {
        logger.info("GET /api/patients/search/dni - Obtener paciente por DNI");

        PatientResponse response = patientService.getByDni(dni);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Paciente obtenido por DNI"));
    }

    /**
     * Lista todos los pacientes
     * GET /api/patients
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAll() {
        logger.info("GET /api/patients - Listar pacientes");

        List<PatientResponse> response = patientService.getAll();

        return ResponseEntity
                .ok(ApiResponse.success(response, "Pacientes obtenidos: " + response.size()));
    }

    /**
     * Busca pacientes por nombre
     * GET /api/patients/search?firstName=...&lastName=...
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> search(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {
        logger.info("GET /api/patients/search - Buscar pacientes");

        List<PatientResponse> response = patientService.searchByName(firstName, lastName);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Pacientes encontrados: " + response.size()));
    }

    /**
     * Desactiva un paciente
     * DELETE /api/patients/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivate(@PathVariable Long id) {
        logger.info("DELETE /api/patients/{} - Desactivar paciente", id);

        patientService.deactivate(id);

        return ResponseEntity
                .ok(ApiResponse.success("Paciente desactivado exitosamente"));
    }
}
