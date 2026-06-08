package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.LoginRequest;
import com.aviva.appointmentsystem.dto.LoginResponse;
import com.aviva.appointmentsystem.dto.PatientResponse;
import com.aviva.appointmentsystem.dto.RegisterPatientRequest;
import com.aviva.appointmentsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticación.
 * 
 * Endpoints PÚBLICOS (no requieren JWT):
 * - POST /api/auth/login          → Autenticación de cualquier usuario
 * - POST /api/auth/register-patient → Auto-registro de pacientes desde el portal web
 * 
 * Reglas arquitectónicas:
 * - El Controller SOLO recibe DTOs (nunca entidades JPA)
 * - Usa @Valid para detonar la validación antes de llamar al Service
 * - NO resuelve relaciones ni hace lógica de negocio (eso es del Service)
 * - Delega TODA la lógica al AuthService
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints de login y registro de pacientes")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint de login.
     * Autentica cualquier tipo de usuario (ADMIN, DOCTOR, RECEPTIONIST, PATIENT).
     * 
     * POST /api/auth/login
     * Body: { "username": "admin", "password": "1234" }
     * Response: { "success": true, "data": { "token": "...", "role": "ADMIN", ... } }
     */
    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario y devuelve un token JWT junto con su rol y datos básicos"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Login exitoso"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Credenciales incorrectas"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Usuario no encontrado"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        logger.info("POST /api/auth/login - Usuario: {}", request.username());

        // Delegar TODA la lógica al Service
        LoginResponse loginResponse = authService.login(request.username(), request.password());

        return ResponseEntity.ok(
            ApiResponse.success(loginResponse, "Autenticación exitosa")
        );
    }

    /**
     * Auto-registro de pacientes desde el portal web.
     * Crea un User con rol PATIENT y un Patient con sus datos personales.
     * 
     * POST /api/auth/register-patient
     * Body: { "username": "jperez", "password": "1234", "dni": "12345678", ... }
     * Response: { "success": true, "data": { "id": 1, "dni": "12345678", ... } }
     */
    @Operation(
        summary = "Registrar paciente (auto-registro)",
        description = "Crea un nuevo usuario con rol PATIENT y su perfil de paciente asociado"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Paciente registrado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos de entrada inválidos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Ya existe un usuario/paciente con esos datos"
        )
    })
    @PostMapping("/register-patient")
    public ResponseEntity<ApiResponse<PatientResponse>> registerPatient(
            @Valid @RequestBody RegisterPatientRequest request) {

        logger.info("POST /api/auth/register-patient - Usuario: {}, DNI: {}",
            request.username(), request.dni());

        // Delegar TODA la lógica al Service
        PatientResponse response = authService.registerPatient(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Paciente registrado exitosamente"));
    }
}