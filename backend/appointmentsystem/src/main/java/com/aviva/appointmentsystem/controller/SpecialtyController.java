package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.SpecialtyRequest;
import com.aviva.appointmentsystem.dto.SpecialtyResponse;
import com.aviva.appointmentsystem.service.SpecialtyService;
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
 * Controlador de especialidades médicas.
 * 
 * Endpoints protegidos (requieren JWT):
 * - GET    /api/specialties      → Listar todas las especialidades activas
 * - GET    /api/specialties/{id} → Obtener especialidad por ID
 * - POST   /api/specialties      → Crear nueva especialidad
 * - PUT    /api/specialties/{id} → Actualizar especialidad
 * - DELETE /api/specialties/{id} → Desactivar especialidad (soft delete)
 * 
 * Reglas arquitectónicas:
 * - El Controller SOLO recibe DTOs (nunca entidades JPA)
 * - Usa @Valid para detonar la validación antes de llamar al Service
 * - NO resuelve relaciones ni hace lógica de negocio
 * - Delega TODA la lógica al SpecialtyService
 */
@RestController
@RequestMapping("/api/specialties")
@Tag(name = "Especialidades", description = "CRUD de especialidades médicas")
@SecurityRequirement(name = "bearerAuth")
public class SpecialtyController {

    private static final Logger logger = LoggerFactory.getLogger(SpecialtyController.class);

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    /**
     * Lista todas las especialidades activas.
     * GET /api/specialties
     */
    @Operation(
        summary = "Listar especialidades",
        description = "Devuelve todas las especialidades médicas activas del sistema"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Lista de especialidades obtenida exitosamente"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<SpecialtyResponse>>> getAll() {
        logger.info("GET /api/specialties - Listar especialidades");

        List<SpecialtyResponse> response = specialtyService.getAll();

        return ResponseEntity.ok(
            ApiResponse.success(response, "Especialidades obtenidas: " + response.size())
        );
    }

    /**
     * Obtiene una especialidad por ID.
     * GET /api/specialties/{id}
     */
    @Operation(
        summary = "Obtener especialidad por ID",
        description = "Busca y devuelve una especialidad médica por su identificador"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Especialidad encontrada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Especialidad no encontrada"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecialtyResponse>> getById(
            @Parameter(description = "ID de la especialidad", required = true)
            @PathVariable Long id) {

        logger.info("GET /api/specialties/{} - Obtener especialidad", id);

        SpecialtyResponse response = specialtyService.getById(id);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Especialidad obtenida")
        );
    }

    /**
     * Crea una nueva especialidad.
     * POST /api/specialties
     * Body: { "name": "Cardiología", "description": "..." }
     */
    @Operation(
        summary = "Crear especialidad",
        description = "Registra una nueva especialidad médica en el sistema"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Especialidad creada exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos de entrada inválidos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Ya existe una especialidad con ese nombre"
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SpecialtyResponse>> create(
            @Valid @RequestBody SpecialtyRequest request) {

        logger.info("POST /api/specialties - Crear especialidad: {}", request.name());

        SpecialtyResponse response = specialtyService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Especialidad creada exitosamente"));
    }

    /**
     * Actualiza una especialidad existente.
     * PUT /api/specialties/{id}
     * Body: { "name": "Cardiología", "description": "..." }
     */
    @Operation(
        summary = "Actualizar especialidad",
        description = "Modifica los datos de una especialidad médica existente"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Especialidad actualizada exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos de entrada inválidos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Especialidad no encontrada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Ya existe otra especialidad con ese nombre"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecialtyResponse>> update(
            @Parameter(description = "ID de la especialidad", required = true)
            @PathVariable Long id,
            @Valid @RequestBody SpecialtyRequest request) {

        logger.info("PUT /api/specialties/{} - Actualizar especialidad", id);

        SpecialtyResponse response = specialtyService.update(id, request);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Especialidad actualizada exitosamente")
        );
    }

    /**
     * Desactiva una especialidad (soft delete).
     * DELETE /api/specialties/{id}
     */
    @Operation(
        summary = "Desactivar especialidad",
        description = "Desactiva una especialidad médica (no la elimina físicamente)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Especialidad desactivada exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Especialidad no encontrada"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivate(
            @Parameter(description = "ID de la especialidad", required = true)
            @PathVariable Long id) {

        logger.info("DELETE /api/specialties/{} - Desactivar especialidad", id);

        specialtyService.deactivate(id);

        return ResponseEntity.ok(
            ApiResponse.success("Especialidad desactivada exitosamente")
        );
    }
}
