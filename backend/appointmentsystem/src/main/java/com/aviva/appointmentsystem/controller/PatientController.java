package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.PatientRequest;
import com.aviva.appointmentsystem.dto.PatientResponse;
import com.aviva.appointmentsystem.service.PatientService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de pacientes.
 * 
 * Endpoints protegidos (requieren JWT):
 * - GET    /api/patients              → Listar todos los pacientes activos
 * - GET    /api/patients/{id}         → Obtener paciente por ID
 * - GET    /api/patients/search/dni   → Buscar paciente por DNI
 * - GET    /api/patients/search       → Buscar pacientes por nombre/apellido
 * - POST   /api/patients              → Crear paciente (uso interno del personal)
 * - PUT    /api/patients/{id}         → Actualizar paciente
 * - DELETE /api/patients/{id}         → Desactivar paciente (soft delete)
 * 
 * NOTA: El auto-registro de pacientes (portal web) se hace en AuthController.
 * 
 * Reglas arquitectónicas:
 * - El Controller SOLO recibe DTOs (nunca entidades JPA)
 * - Usa @Valid para detonar la validación antes de llamar al Service
 * - NO resuelve relaciones ni hace lógica de negocio
 * - Delega TODA la lógica al PatientService
 */
@RestController
@RequestMapping("/api/patients")
@Tag(name = "Pacientes", description = "CRUD y búsquedas de pacientes")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Lista todos los pacientes activos.
     * GET /api/patients
     */
    @Operation(
        summary = "Listar pacientes",
        description = "Devuelve todos los pacientes activos del sistema"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Lista de pacientes obtenida exitosamente"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getAll() {
        logger.info("GET /api/patients - Listar pacientes");

        List<PatientResponse> response = patientService.getAll();

        return ResponseEntity.ok(
            ApiResponse.success(response, "Pacientes obtenidos: " + response.size())
        );
    }

    /**
     * Obtiene un paciente por ID.
     * GET /api/patients/{id}
     */
    @Operation(
        summary = "Obtener paciente por ID",
        description = "Busca y devuelve un paciente por su identificador"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Paciente encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Paciente no encontrado"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> getById(
            @Parameter(description = "ID del paciente", required = true)
            @PathVariable Long id) {

        logger.info("GET /api/patients/{} - Obtener paciente", id);

        PatientResponse response = patientService.getById(id);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Paciente obtenido")
        );
    }

    /**
     * Busca un paciente por DNI.
     * GET /api/patients/search/dni?dni=12345678
     */
    @Operation(
        summary = "Buscar paciente por DNI",
        description = "Busca y devuelve un paciente por su número de DNI (8 dígitos)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Paciente encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "No se encontró paciente con ese DNI"
        )
    })
    @GetMapping("/search/dni")
    public ResponseEntity<ApiResponse<PatientResponse>> getByDni(
            @Parameter(description = "DNI del paciente (8 dígitos)", required = true, example = "12345678")
            @RequestParam String dni) {

        logger.info("GET /api/patients/search/dni?dni={}", dni);

        PatientResponse response = patientService.getByDni(dni);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Paciente encontrado por DNI")
        );
    }

    /**
     * Busca pacientes por nombre y/o apellido.
     * GET /api/patients/search?firstName=Juan&lastName=Perez
     * 
     * Ambos parámetros son opcionales. Si no se envía ninguno,
     * se devuelven todos los pacientes activos.
     */
    @Operation(
        summary = "Buscar pacientes por nombre",
        description = "Búsqueda flexible por nombre y/o apellido (parcial, case-insensitive). " +
                      "Si no se envían parámetros, devuelve todos los pacientes activos."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Resultados de búsqueda obtenidos"
        )
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> search(
            @Parameter(description = "Nombre del paciente (parcial)", example = "Juan")
            @RequestParam(required = false) String firstName,
            @Parameter(description = "Apellido del paciente (parcial)", example = "Perez")
            @RequestParam(required = false) String lastName) {

        logger.info("GET /api/patients/search?firstName={}&lastName={}", firstName, lastName);

        List<PatientResponse> response = patientService.searchByName(firstName, lastName);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Pacientes encontrados: " + response.size())
        );
    }

    /**
     * Crea un nuevo paciente (uso interno del personal).
     * POST /api/patients
     * 
     * NOTA: Para auto-registro de pacientes desde el portal web
     * (que crea User + Patient), usar POST /api/auth/register-patient
     */
    @Operation(
        summary = "Crear paciente (uso interno)",
        description = "Registra un nuevo paciente. Este endpoint es para uso interno del personal. " +
                      "Para auto-registro de pacientes, usar /api/auth/register-patient"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Paciente creado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos de entrada inválidos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Ya existe un paciente con ese DNI o email"
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PatientResponse>> create(
            @Valid @RequestBody PatientRequest request) {

        logger.info("POST /api/patients - Crear paciente con DNI: {}", request.dni());

        PatientResponse response = patientService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Paciente creado exitosamente"));
    }

    /**
     * Actualiza un paciente existente.
     * PUT /api/patients/{id}
     */
    @Operation(
        summary = "Actualizar paciente",
        description = "Modifica los datos personales de un paciente existente"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Paciente actualizado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos de entrada inválidos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Paciente no encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Ya existe otro paciente con ese DNI o email"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> update(
            @Parameter(description = "ID del paciente", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {

        logger.info("PUT /api/patients/{} - Actualizar paciente", id);

        PatientResponse response = patientService.update(id, request);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Paciente actualizado exitosamente")
        );
    }

    /**
     * Desactiva un paciente (soft delete).
     * DELETE /api/patients/{id}
     */
    @Operation(
        summary = "Desactivar paciente",
        description = "Desactiva un paciente del sistema (no lo elimina físicamente)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Paciente desactivado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Paciente no encontrado"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivate(
            @Parameter(description = "ID del paciente", required = true)
            @PathVariable Long id) {

        logger.info("DELETE /api/patients/{} - Desactivar paciente", id);

        patientService.deactivate(id);

        return ResponseEntity.ok(
            ApiResponse.success("Paciente desactivado exitosamente")
        );
    }
}
