package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.AppointmentRequest;
import com.aviva.appointmentsystem.dto.AppointmentResponse;
import com.aviva.appointmentsystem.dto.AvailableSlotResponse;
import com.aviva.appointmentsystem.entity.AppointmentStatus;
import com.aviva.appointmentsystem.service.AppointmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;


/**
 * Controlador de citas médicas
 * Maneja endpoints para CRUD de citas y sus operaciones
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;

    /**
     * Crea una nueva cita médica
     * POST /api/appointments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(
            @Valid @RequestBody AppointmentRequest request) {
        logger.info("POST /api/appointments - Crear cita");

        AppointmentResponse response = appointmentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cita creada exitosamente"));
    }

    /**
     * Reprograma una cita a una nueva fecha/hora
     * PUT /api/appointments/{id}/reschedule?newDateTime=...
     */
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<AppointmentResponse>> reschedule(
            @PathVariable Long id,
            @RequestParam String newDateTime) {
        logger.info("PUT /api/appointments/{}/reschedule - Reprogramar cita", id);

        AppointmentResponse response = appointmentService.reschedule(id, newDateTime);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Cita reprogramada exitosamente"));
    }

    /**
     * Cancela una cita
     * PUT /api/appointments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(@PathVariable Long id) {
        logger.info("PUT /api/appointments/{}/cancel - Cancelar cita", id);

        AppointmentResponse response = appointmentService.cancel(id);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Cita cancelada exitosamente"));
    }

    /**
     * Obtiene una cita por ID
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getById(@PathVariable Long id) {
        logger.info("GET /api/appointments/{} - Obtener cita", id);

        AppointmentResponse response = appointmentService.getById(id);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Cita obtenida"));
    }

    /**
     * Lista todas las citas
     * GET /api/appointments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAll() {
        logger.info("GET /api/appointments - Listar citas");

        List<AppointmentResponse> response = appointmentService.getAll();

        return ResponseEntity
                .ok(ApiResponse.success(response, "Citas obtenidas: " + response.size()));
    }

    /**
     * Obtiene citas de un paciente
     * GET /api/appointments/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByPatient(
            @PathVariable Long patientId) {
        logger.info("GET /api/appointments/patient/{} - Obtener citas del paciente", patientId);

        List<AppointmentResponse> response = appointmentService.getByPatient(patientId);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Citas encontradas: " + response.size()));
    }

    /**
     * Obtiene citas de un doctor
     * GET /api/appointments/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByDoctor(
            @PathVariable Long doctorId) {
        logger.info("GET /api/appointments/doctor/{} - Obtener citas del doctor", doctorId);

        List<AppointmentResponse> response = appointmentService.getByDoctor(doctorId);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Citas encontradas: " + response.size()));
    }

    /**
     * Obtiene citas por estado
     * GET /api/appointments/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByStatus(
            @PathVariable String status) {
        logger.info("GET /api/appointments/status/{} - Obtener citas por estado", status);

        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
        List<AppointmentResponse> response = appointmentService.getByStatus(appointmentStatus);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Citas encontradas: " + response.size()));
    }

    /**
   * Obtiene slots disponibles de un doctor para una fecha específica
   * GET /api/appointments/doctor/{doctorId}/available-slots?date=2024-12-20
   */
   @GetMapping("/doctor/{doctorId}/available-slots")
   public ResponseEntity<ApiResponse<List<AvailableSlotResponse>>> getAvailableSlots(
        @PathVariable Long doctorId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
   logger.info("GET /api/appointments/doctor/{}/available-slots - fecha={}", doctorId, date);

   List<AvailableSlotResponse> response = appointmentService.getAvailableSlots(doctorId, date);

        return ResponseEntity
        .ok(ApiResponse.success(response, "Slots disponibles: " + response.size()));
}
}
