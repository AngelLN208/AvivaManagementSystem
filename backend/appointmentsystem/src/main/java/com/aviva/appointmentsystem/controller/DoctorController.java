package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.DoctorRequest;
import com.aviva.appointmentsystem.dto.DoctorResponse;
import com.aviva.appointmentsystem.dto.DoctorUpdateRequest;
import com.aviva.appointmentsystem.service.DoctorService;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Controlador de médicos.
 * 
 * Endpoints protegidos (requieren JWT):
 * - GET    /api/doctors                         → Listar todos los médicos activos
 * - GET    /api/doctors/{id}                    → Obtener médico por ID
 * - GET    /api/doctors/by-specialty/{specialtyId} → Médicos por especialidad
 * - POST   /api/doctors                         → Crear médico (solo ADMIN — RN-41)
 * - PUT    /api/doctors/{id}                    → Actualizar médico (solo ADMIN — RN-41)
 * - DELETE /api/doctors/{id}                    → Desactivar médico (solo ADMIN — RN-41)
 * 
 * Reglas arquitectónicas:
 * - El Controller SOLO recibe DTOs (nunca entidades JPA)
 * - Usa @Valid para detonar validación antes de llamar al Service
 * - NO resuelve relaciones ni hace lógica de negocio
 * - Delega TODA la lógica al DoctorService
 */
@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Médicos", description = "CRUD de médicos y búsqueda por especialidad")
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {

    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /**
     * Lista todos los médicos activos.
     * GET /api/doctors
     */
    @Operation(
        summary = "Listar médicos",
        description = "Devuelve todos los médicos activos con su especialidad anidada"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Lista de médicos obtenida exitosamente"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAll() {
        logger.info("GET /api/doctors - Listar médicos");

        List<DoctorResponse> response = doctorService.getAll();

        return ResponseEntity.ok(
            ApiResponse.success(response, "Doctores obtenidos: " + response.size())
        );
    }

    /**
     * Obtiene un médico por ID.
     * GET /api/doctors/{id}
     */
    @Operation(
        summary = "Obtener médico por ID",
        description = "Busca y devuelve un médico con su especialidad anidada"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Médico encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Médico no encontrado"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> getById(
            @Parameter(description = "ID del médico", required = true)
            @PathVariable Long id) {

        logger.info("GET /api/doctors/{} - Obtener médico", id);

        DoctorResponse response = doctorService.getById(id);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Doctor obtenido")
        );
    }

    /**
     * Obtiene médicos activos por especialidad.
     * GET /api/doctors/by-specialty/{specialtyId}
     */
    @Operation(
        summary = "Médicos por especialidad",
        description = "Devuelve todos los médicos activos de una especialidad determinada (RN-36)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Médicos encontrados"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Especialidad no encontrada"
        )
    })
    @GetMapping("/by-specialty/{specialtyId}")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getBySpecialty(
            @Parameter(description = "ID de la especialidad", required = true, example = "1")
            @PathVariable Long specialtyId) {

        logger.info("GET /api/doctors/by-specialty/{} - Médicos por especialidad", specialtyId);

        List<DoctorResponse> response = doctorService.getBySpecialty(specialtyId);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Doctores encontrados: " + response.size())
        );
    }

    /**
     * Crea un nuevo médico.
     * POST /api/doctors
     * 
     * RN-41: Solo el administrador puede registrar médicos.
     */
    @Operation(
        summary = "Crear médico (solo ADMIN)",
        description = "Registra un nuevo médico asociado a una especialidad existente. " +
                      "RN-36: specialtyId debe existir. RN-41: Requiere rol ADMIN."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Médico creado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos de entrada inválidos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Especialidad no encontrada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Ya existe un médico con esa licencia o email"
        )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> create(
            @Valid @RequestBody DoctorRequest request) {

        logger.info("POST /api/doctors - Crear médico: {} {}", request.firstName(), request.lastName());

        DoctorResponse response = doctorService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Doctor creado exitosamente"));
    }

    /**
     * Actualiza un médico existente (actualización parcial).
     * PUT /api/doctors/{id}
     * 
     * RN-41: Solo el administrador puede actualizar médicos.
     */
    @Operation(
        summary = "Actualizar médico (solo ADMIN)",
        description = "Modifica parcialmente los datos de un médico. Solo se actualizan los campos enviados. " +
                      "RN-41: Requiere rol ADMIN."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Médico actualizado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos de entrada inválidos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Médico o especialidad no encontrada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Ya existe otro médico con esa licencia o email"
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> update(
            @Parameter(description = "ID del médico", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequest request) {

        logger.info("PUT /api/doctors/{} - Actualizar médico", id);

        DoctorResponse response = doctorService.update(id, request);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Doctor actualizado exitosamente")
        );
    }

    /**
     * Desactiva un médico (soft delete).
     * DELETE /api/doctors/{id}
     * 
     * RN-41: Solo el administrador puede desactivar médicos.
     */
    @Operation(
        summary = "Desactivar médico (solo ADMIN)",
        description = "Desactiva un médico del sistema (no lo elimina físicamente). " +
                      "RN-41: Requiere rol ADMIN."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Médico desactivado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Médico no encontrado"
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivate(
            @Parameter(description = "ID del médico", required = true)
            @PathVariable Long id) {

        logger.info("DELETE /api/doctors/{} - Desactivar médico", id);

        doctorService.deactivate(id);

        return ResponseEntity.ok(
            ApiResponse.success("Doctor desactivado exitosamente")
        );
    }
}
