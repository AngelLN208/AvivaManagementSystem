package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.TriageRequest;
import com.aviva.appointmentsystem.dto.TriageResponse;
import com.aviva.appointmentsystem.service.TriageService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/triages")
@Tag(name = "Triaje", description = "Registro y consulta de signos vitales por cita médica")
@SecurityRequirement(name = "bearerAuth")
public class TriageController {

    private static final Logger logger = LoggerFactory.getLogger(TriageController.class);

    private final TriageService triageService;

    public TriageController(TriageService triageService) {
        this.triageService = triageService;
    }

    @Operation(summary = "Registrar triaje",
        description = "Registra signos vitales. Rangos: sistólica 70-200, diastólica 40-130, " +
            "temperatura 34-42°C, FC 30-220, FR 8-60, peso 2-500 kg, altura 30-250 cm. " +
            "Solo se permite un triaje por cita.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Triaje registrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Valores fuera de rango clínico"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cita no encontrada"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ya existe triaje para esta cita")
    })
    @PostMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<TriageResponse>> create(
            @Parameter(description = "ID de la cita", required = true, example = "1")
            @PathVariable Long appointmentId,
            @Valid @RequestBody TriageRequest request) {

        logger.info("POST /api/triages/{}", appointmentId);
        TriageResponse response = triageService.create(appointmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Triaje registrado exitosamente"));
    }

    @Operation(summary = "Obtener triaje de cita",
        description = "Devuelve los signos vitales registrados para una cita")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Triaje obtenido"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cita o triaje no encontrado")
    })
    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<TriageResponse>> getByAppointment(
            @Parameter(description = "ID de la cita", required = true, example = "1")
            @PathVariable Long appointmentId) {

        logger.info("GET /api/triages/{}", appointmentId);
        TriageResponse response = triageService.getByAppointment(appointmentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Triaje obtenido"));
    }
}
