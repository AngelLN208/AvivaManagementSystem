package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.MedicalScheduleRequest;
import com.aviva.appointmentsystem.dto.MedicalScheduleResponse;
import com.aviva.appointmentsystem.dto.MedicalScheduleUpdateRequest;
import com.aviva.appointmentsystem.service.MedicalScheduleService;
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

import java.time.DayOfWeek;
import java.util.List;

/**
 * Controlador de horarios médicos.
 * 
 * Endpoints protegidos (requieren JWT):
 * - GET    /api/medical-schedules/doctor/{doctorId}      → Horarios de un doctor
 * - GET    /api/medical-schedules/doctor/{doctorId}/day  → Horarios por día (RN-38)
 * - POST   /api/medical-schedules/doctor/{doctorId}      → Crear horario (RN-37)
 * - PUT    /api/medical-schedules/{scheduleId}            → Actualizar horario
 * - DELETE /api/medical-schedules/{scheduleId}            → Desactivar horario
 * 
 * Reglas arquitectónicas:
 * - El Controller SOLO recibe DTOs (nunca entidades JPA)
 * - Usa @Valid para detonar validación antes de llamar al Service
 * - NO resuelve relaciones ni hace lógica de negocio
 * - Delega TODA la lógica al MedicalScheduleService
 */
@RestController
@RequestMapping("/api/medical-schedules")
@Tag(name = "Horarios Médicos", description = "Gestión de horarios de disponibilidad de médicos (RN-37, RN-38)")
@SecurityRequirement(name = "bearerAuth")
public class MedicalScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(MedicalScheduleController.class);

    private final MedicalScheduleService medicalScheduleService;

    public MedicalScheduleController(MedicalScheduleService medicalScheduleService) {
        this.medicalScheduleService = medicalScheduleService;
    }

    /**
     * Obtiene todos los horarios activos de un doctor.
     * GET /api/medical-schedules/doctor/{doctorId}
     */
    @Operation(
        summary = "Horarios de un doctor",
        description = "Devuelve todos los horarios activos asignados a un médico"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Horarios obtenidos exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Doctor no encontrado"
        )
    })
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<MedicalScheduleResponse>>> getDoctorSchedules(
            @Parameter(description = "ID del doctor", required = true, example = "1")
            @PathVariable Long doctorId) {

        logger.info("GET /api/medical-schedules/doctor/{} - Obtener horarios", doctorId);

        List<MedicalScheduleResponse> response = medicalScheduleService.getDoctorSchedules(doctorId);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Horarios obtenidos: " + response.size())
        );
    }

    /**
     * Obtiene el horario de un doctor para un día específico.
     * GET /api/medical-schedules/doctor/{doctorId}/day?day=MONDAY
     * 
     * RN-38: Se usa para validar disponibilidad de citas.
     */
    @Operation(
        summary = "Horarios por día",
        description = "Devuelve los horarios activos de un médico para un día específico de la semana (RN-38)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Horarios del día obtenidos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Doctor no encontrado"
        )
    })
    @GetMapping("/doctor/{doctorId}/day")
    public ResponseEntity<ApiResponse<List<MedicalScheduleResponse>>> getDoctorScheduleByDay(
            @Parameter(description = "ID del doctor", required = true, example = "1")
            @PathVariable Long doctorId,
            @Parameter(description = "Día de la semana", required = true, example = "MONDAY")
            @RequestParam DayOfWeek day) {

        logger.info("GET /api/medical-schedules/doctor/{}/day?day={}", doctorId, day);

        List<MedicalScheduleResponse> response = medicalScheduleService.getDoctorScheduleByDay(doctorId, day);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Horarios del " + day + ": " + response.size())
        );
    }

    /**
     * Crea un horario para un doctor.
     * POST /api/medical-schedules/doctor/{doctorId}
     * 
     * RN-37: Define los horarios en que el doctor atiende.
     * Valida que no haya superposición con horarios existentes.
     */
    @Operation(
        summary = "Crear horario",
        description = "Asigna un nuevo horario de atención a un médico. " +
                      "RN-37: Valida que el doctor exista y que no haya superposición de horarios."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Horario creado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos inválidos o startTime >= endTime o superposición detectada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Doctor no encontrado"
        )
    })
    @PostMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<MedicalScheduleResponse>> create(
            @Parameter(description = "ID del doctor", required = true, example = "1")
            @PathVariable Long doctorId,
            @Valid @RequestBody MedicalScheduleRequest request) {

        logger.info("POST /api/medical-schedules/doctor/{} - Crear horario: {}",
            doctorId, request.dayOfWeek());

        MedicalScheduleResponse response = medicalScheduleService.create(doctorId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Horario creado exitosamente"));
    }

    /**
     * Actualiza un horario existente (actualización parcial).
     * PUT /api/medical-schedules/{scheduleId}
     * 
     * Solo se actualizan los campos enviados.
     * Re-valida startTime < endTime y ausencia de superposición con los valores finales.
     */
    @Operation(
        summary = "Actualizar horario",
        description = "Modifica parcialmente un horario. Solo se actualizan los campos enviados. " +
                      "Re-valida que no haya superposición de horarios."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Horario actualizado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos inválidos o superposición detectada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Horario no encontrado"
        )
    })
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<MedicalScheduleResponse>> update(
            @Parameter(description = "ID del horario", required = true)
            @PathVariable Long scheduleId,
            @Valid @RequestBody MedicalScheduleUpdateRequest request) {

        logger.info("PUT /api/medical-schedules/{} - Actualizar horario", scheduleId);

        MedicalScheduleResponse response = medicalScheduleService.update(scheduleId, request);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Horario actualizado exitosamente")
        );
    }

    /**
     * Desactiva un horario (soft delete).
     * DELETE /api/medical-schedules/{scheduleId}
     */
    @Operation(
        summary = "Desactivar horario",
        description = "Desactiva un horario de atención (no lo elimina físicamente)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Horario desactivado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Horario no encontrado"
        )
    })
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<String>> deactivate(
            @Parameter(description = "ID del horario", required = true)
            @PathVariable Long scheduleId) {

        logger.info("DELETE /api/medical-schedules/{} - Desactivar horario", scheduleId);

        medicalScheduleService.deactivate(scheduleId);

        return ResponseEntity.ok(
            ApiResponse.success("Horario desactivado exitosamente")
        );
    }
}
