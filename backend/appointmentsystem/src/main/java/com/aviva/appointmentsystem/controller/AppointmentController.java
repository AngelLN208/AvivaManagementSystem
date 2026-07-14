package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.AppointmentRequest;
import com.aviva.appointmentsystem.dto.AppointmentResponse;
import com.aviva.appointmentsystem.dto.AvailableSlotResponse;
import com.aviva.appointmentsystem.dto.PatientAppointmentRequest;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de citas médicas.
 *
 * Endpoints:
 * - GET    /api/appointments                              → Listar todas las citas
 * - GET    /api/appointments/{id}                         → Obtener cita por ID
 * - GET    /api/appointments/patient/{patientId}          → Citas de un paciente
 * - GET    /api/appointments/doctor/{doctorId}            → Citas de un doctor
 * - GET    /api/appointments/status/{status}              → Citas por estado
 * - GET    /api/appointments/doctor/{doctorId}/available-slots → Slots disponibles (RN-38)
 * - POST   /api/appointments                              → Crear cita (RN-13, RN-38, RN-12, RN-21)
 * - PUT    /api/appointments/{id}/reschedule              → Reprogramar (RN-14)
 * - PUT    /api/appointments/{id}/cancel                  → Cancelar (RN-15)
 *
 * Reglas arquitectónicas:
 * - Controller SOLO recibe DTOs, delega toda lógica al AppointmentService
 * - @Valid detonado antes del Service
 * - No hay lógica de negocio aquí
 */
@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Citas Médicas", description = "Gestión de citas: creación, reprogramación, cancelación y consulta de disponibilidad")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ========================================================
    // PORTAL DEL PACIENTE (/me)
    // ========================================================

    /**
     * Los endpoints /me nunca reciben patientId. El servicio resuelve el
     * perfil usando el username autenticado y valida la propiedad de la cita.
     */
    @Operation(
        summary = "Listar mis citas",
        description = "Devuelve únicamente las citas del paciente autenticado"
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMine(Principal principal) {
        logger.info("GET /api/appointments/me - usuario={}", principal.getName());

        List<AppointmentResponse> response =
                appointmentService.getForCurrentPatient(principal.getName());

        return ResponseEntity.ok(
                ApiResponse.success(response, "Mis citas obtenidas: " + response.size())
        );
    }

    @Operation(
        summary = "Obtener una de mis citas",
        description = "Devuelve la cita solo cuando pertenece al paciente autenticado"
    )
    @GetMapping("/me/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getMineById(
            Principal principal,
            @PathVariable Long id) {

        logger.info("GET /api/appointments/me/{} - usuario={}", id, principal.getName());

        AppointmentResponse response =
                appointmentService.getByIdForCurrentPatient(principal.getName(), id);

        return ResponseEntity.ok(ApiResponse.success(response, "Cita obtenida"));
    }

    @Operation(
        summary = "Crear mi cita",
        description = "Crea una cita para el paciente autenticado; no acepta patientId"
    )
    @PostMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createMine(
            Principal principal,
            @Valid @RequestBody PatientAppointmentRequest request) {

        logger.info("POST /api/appointments/me - usuario={}, doctor={}, hora={}",
                principal.getName(), request.doctorId(), request.appointmentDateTime());

        AppointmentResponse response =
                appointmentService.createForCurrentPatient(principal.getName(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cita creada exitosamente"));
    }

    @Operation(
        summary = "Reprogramar mi cita",
        description = "Reprograma la cita solo cuando pertenece al paciente autenticado"
    )
    @PutMapping("/me/{id}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleMine(
            Principal principal,
            @PathVariable Long id,
            @RequestParam String newDateTime) {

        logger.info("PUT /api/appointments/me/{}/reschedule - usuario={}",
                id, principal.getName());

        AppointmentResponse response = appointmentService.rescheduleForCurrentPatient(
                principal.getName(), id, newDateTime);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Cita reprogramada exitosamente")
        );
    }

    @Operation(
        summary = "Cancelar mi cita",
        description = "Cancela la cita solo cuando pertenece al paciente autenticado"
    )
    @PutMapping("/me/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelMine(
            Principal principal,
            @PathVariable Long id) {

        logger.info("PUT /api/appointments/me/{}/cancel - usuario={}",
                id, principal.getName());

        AppointmentResponse response =
                appointmentService.cancelForCurrentPatient(principal.getName(), id);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Cita cancelada exitosamente")
        );
    }

    // ========================================================
    // CONSULTAS (GET)
    // ========================================================

    /**
     * Lista todas las citas del sistema.
     * GET /api/appointments
     */
    @Operation(
        summary = "Listar todas las citas",
        description = "Devuelve todas las citas del sistema con datos anidados de paciente y doctor"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Lista de citas obtenida exitosamente"
        )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAll() {
        logger.info("GET /api/appointments - Listar citas");

        List<AppointmentResponse> response = appointmentService.getAll();

        return ResponseEntity.ok(
            ApiResponse.success(response, "Citas obtenidas: " + response.size())
        );
    }

    /**
     * Obtiene una cita por ID.
     * GET /api/appointments/{id}
     */
    @Operation(
        summary = "Obtener cita por ID",
        description = "Devuelve los detalles completos de una cita incluyendo paciente y doctor"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Cita encontrada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Cita no encontrada"
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getById(
            @Parameter(description = "ID de la cita", required = true, example = "1")
            @PathVariable Long id) {

        logger.info("GET /api/appointments/{}", id);

        AppointmentResponse response = appointmentService.getById(id);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Cita obtenida")
        );
    }

    /**
     * Lista citas de un paciente.
     * GET /api/appointments/patient/{patientId}
     */
    @Operation(
        summary = "Citas de un paciente",
        description = "Devuelve todas las citas de un paciente (todos los estados)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Citas del paciente obtenidas"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Paciente no encontrado"
        )
    })
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByPatient(
            @Parameter(description = "ID del paciente", required = true, example = "1")
            @PathVariable Long patientId) {

        logger.info("GET /api/appointments/patient/{}", patientId);

        List<AppointmentResponse> response = appointmentService.getByPatient(patientId);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Citas del paciente: " + response.size())
        );
    }

    /**
     * Lista citas de un doctor.
     * GET /api/appointments/doctor/{doctorId}
     */
    @Operation(
        summary = "Citas de un doctor",
        description = "Devuelve todas las citas asignadas a un doctor (todos los estados)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Citas del doctor obtenidas"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Doctor no encontrado"
        )
    })
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByDoctor(
            @Parameter(description = "ID del doctor", required = true, example = "1")
            @PathVariable Long doctorId) {

        logger.info("GET /api/appointments/doctor/{}", doctorId);

        List<AppointmentResponse> response = appointmentService.getByDoctor(doctorId);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Citas del doctor: " + response.size())
        );
    }

    /**
     * Lista citas por estado.
     * GET /api/appointments/status/{status}
     */
    @Operation(
        summary = "Citas por estado",
        description = "Filtra citas por su estado actual. " +
                      "Estados válidos: PENDING, CONFIRMED, COMPLETED, CANCELLED, RESCHEDULED, NO_SHOW"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Citas filtradas por estado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Estado inválido"
        )
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByStatus(
            @Parameter(
                description = "Estado de la cita",
                required = true,
                example = "PENDING"
            )
            @PathVariable String status) {

        logger.info("GET /api/appointments/status/{}", status);

        AppointmentStatus appointmentStatus;
        try {
            appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Estado inválido: '" + status + "'. " +
                "Valores válidos: PENDING, CONFIRMED, COMPLETED, CANCELLED, RESCHEDULED, NO_SHOW"
            );
        }

        List<AppointmentResponse> response = appointmentService.getByStatus(appointmentStatus);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Citas con estado " + status + ": " + response.size())
        );
    }

    /**
     * Calcula los slots de tiempo disponibles de un doctor para una fecha.
     * GET /api/appointments/doctor/{doctorId}/available-slots?date=2026-05-20
     *
     * RN-38: Solo muestra slots dentro del horario activo del doctor.
     * Excluye slots ocupados por citas activas (PENDING/CONFIRMED/RESCHEDULED).
     */
    @Operation(
        summary = "Slots disponibles",
        description = "Calcula los horarios libres de un doctor para una fecha. " +
                      "RN-38: Basado en sus MedicalSchedules activos. " +
                      "Descuenta los slots ya ocupados por citas PENDING/CONFIRMED/RESCHEDULED."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Slots calculados. Lista vacía si no hay disponibilidad"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Doctor no encontrado"
        )
    })
    @GetMapping("/doctor/{doctorId}/available-slots")
    public ResponseEntity<ApiResponse<List<AvailableSlotResponse>>> getAvailableSlots(
            @Parameter(description = "ID del doctor", required = true, example = "1")
            @PathVariable Long doctorId,
            @Parameter(description = "Fecha en formato yyyy-MM-dd", required = true, example = "2026-05-20")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        logger.info("GET /api/appointments/doctor/{}/available-slots?date={}", doctorId, date);

        List<AvailableSlotResponse> response = appointmentService.getAvailableSlots(doctorId, date);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Slots disponibles: " + response.size())
        );
    }

    // ========================================================
    // OPERACIONES DE ESTADO (POST / PUT)
    // ========================================================

    /**
     * Crea una nueva cita médica.
     * POST /api/appointments
     *
     * Validaciones (en orden):
     * 1. @Valid: campos del DTO (patientId, doctorId, appointmentDateTime obligatorios)
     * 2. RN-38: Hora dentro del horario del doctor ese día
     * 3. RN-12: Sin conflicto con cita activa del doctor en ese horario
     * 4. RN-13: Estado inicial → PENDING
     * 5. RN-21: Genera Payment PENDING automáticamente
     */
    @Operation(
        summary = "Crear cita",
        description = "Registra una nueva cita médica. " +
                      "RN-13: Estado inicial PENDING. " +
                      "RN-38: La hora debe estar dentro del horario del doctor. " +
                      "RN-12: No debe haber conflicto con otra cita activa. " +
                      "RN-21: Genera un pago en estado PENDING automáticamente."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Cita creada exitosamente con pago PENDING generado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Datos inválidos o fecha en el pasado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Paciente o doctor no encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "RN-38: Hora fuera del horario del doctor, o RN-12: Conflicto de horario"
        )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(
            @Valid @RequestBody AppointmentRequest request) {

        logger.info("POST /api/appointments - Crear cita: paciente={}, doctor={}, hora={}",
            request.patientId(), request.doctorId(), request.appointmentDateTime());

        AppointmentResponse response = appointmentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cita creada exitosamente"));
    }

    /**
     * Reprograma una cita a una nueva fecha/hora.
     * PUT /api/appointments/{id}/reschedule?newDateTime=2026-05-25T11:00:00
     *
     * RN-14: Estado cambia a RESCHEDULED.
     * Re-valida RN-38 y RN-12 para la nueva fecha/hora.
     */
    @Operation(
        summary = "Reprogramar cita",
        description = "Cambia la fecha/hora de una cita existente (RN-14 → estado RESCHEDULED). " +
                      "Re-valida RN-38 (dentro del horario) y RN-12 (sin conflicto) para la nueva fecha."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Cita reprogramada exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Cita no encontrada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Cita cancelada/completada, hora fuera de horario, o conflicto"
        )
    })
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> reschedule(
            @Parameter(description = "ID de la cita", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nueva fecha y hora (yyyy-MM-ddTHH:mm:ss)", required = true, example = "2026-05-25T11:00:00")
            @RequestParam String newDateTime) {

        logger.info("PUT /api/appointments/{}/reschedule?newDateTime={}", id, newDateTime);

        AppointmentResponse response = appointmentService.reschedule(id, newDateTime);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Cita reprogramada exitosamente")
        );
    }

    /**
     * Cancela una cita.
     * PUT /api/appointments/{id}/cancel
     *
     * RN-15: Estado cambia a CANCELLED.
     * No se puede cancelar si ya está CANCELLED o COMPLETED.
     */
    @Operation(
        summary = "Cancelar cita",
        description = "Cancela una cita médica (RN-15 → estado CANCELLED). " +
                      "No se puede cancelar una cita ya cancelada o completada."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Cita cancelada exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Cita no encontrada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Cita ya cancelada o completada"
        )
    })
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(
            @Parameter(description = "ID de la cita", required = true, example = "1")
            @PathVariable Long id) {

        logger.info("PUT /api/appointments/{}/cancel", id);

        AppointmentResponse response = appointmentService.cancel(id);

        return ResponseEntity.ok(
            ApiResponse.success(response, "Cita cancelada exitosamente")
        );
    }
}
