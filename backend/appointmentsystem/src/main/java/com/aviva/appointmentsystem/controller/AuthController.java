package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.LoginRequest;
import com.aviva.appointmentsystem.dto.PatientRequest;
import com.aviva.appointmentsystem.dto.PatientResponse;
import com.aviva.appointmentsystem.service.AuthService;
import com.aviva.appointmentsystem.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de autenticación
 * Maneja endpoints relacionados con login y registro
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private PatientService patientService;

    /**
     * Endpoint de login
     * POST /api/auth/login
     *
     * @param request objeto con username y password
     * @return respuesta con token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("POST /api/auth/login - Iniciar sesión para usuario: {}", request.username());

        String token = authService.authenticate(request.username(), request.password());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", request.username());
        response.put("message", "Login exitoso");

        return ResponseEntity
                .ok(ApiResponse.success(response, "Autenticación exitosa"));
    }

    /**
     * Registro de pacientes (auto-registro)
     * POST /api/auth/register-patient
     *
     * @param request objeto con datos del paciente
     * @return respuesta con datos del paciente registrado
     */
    @PostMapping("/register-patient")
    public ResponseEntity<ApiResponse<PatientResponse>> registerPatient(
            @Valid @RequestBody PatientRequest request) {
        logger.info("POST /api/auth/register-patient - Auto-registro de paciente");

        PatientResponse response = patientService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Paciente registrado exitosamente"));
    }
}