package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.MedicalScheduleRequest;
import com.aviva.appointmentsystem.dto.MedicalScheduleResponse;
import com.aviva.appointmentsystem.service.MedicalScheduleService;
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

import java.time.DayOfWeek;
import java.util.List;

/**
 * Controlador de horarios médicos
 * Gestiona horarios de disponibilidad de doctores (RN-37, RN-38)
 */
@RestController
@RequestMapping("/api/medical-schedules")
public class MedicalScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(MedicalScheduleController.class);

    @Autowired
    private MedicalScheduleService medicalScheduleService;

    /**
     * Crea un horario para un doctor
     * POST /api/medical-schedules/doctor/{doctorId}
     */
    @PostMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<MedicalScheduleResponse>> create(
            @PathVariable Long doctorId,
            @Valid @RequestBody MedicalScheduleRequest request) {
        logger.info("POST /api/medical-schedules/doctor/{} - Crear horario", doctorId);

        MedicalScheduleResponse response = medicalScheduleService.create(doctorId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Horario creado exitosamente"));
    }

    /**
     * Obtiene todos los horarios de un doctor
     * GET /api/medical-schedules/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<MedicalScheduleResponse>>> getDoctorSchedules(
            @PathVariable Long doctorId) {
        logger.info("GET /api/medical-schedules/doctor/{} - Obtener horarios", doctorId);

        List<MedicalScheduleResponse> response = medicalScheduleService.getDoctorSchedules(doctorId);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Horarios obtenidos: " + response.size()));
    }

    /**
     * Obtiene el horario de un doctor para un día específico
     * GET /api/medical-schedules/doctor/{doctorId}/day?day=MONDAY
     */
    @GetMapping("/doctor/{doctorId}/day")
    public ResponseEntity<ApiResponse<List<MedicalScheduleResponse>>> getDoctorScheduleByDay(
            @PathVariable Long doctorId,
            @RequestParam DayOfWeek day) {
        logger.info("GET /api/medical-schedules/doctor/{}/day - Obtener horarios por día", doctorId);

        List<MedicalScheduleResponse> response = medicalScheduleService.getDoctorScheduleByDay(doctorId, day);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Horarios obtenidos: " + response.size()));
    }

    /**
     * Actualiza un horario
     * PUT /api/medical-schedules/{scheduleId}
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<MedicalScheduleResponse>> update(
            @PathVariable Long scheduleId,
            @Valid @RequestBody MedicalScheduleRequest request) {
        logger.info("PUT /api/medical-schedules/{} - Actualizar horario", scheduleId);

        MedicalScheduleResponse response = medicalScheduleService.update(scheduleId, request);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Horario actualizado exitosamente"));
    }

    /**
     * Desactiva un horario
     * DELETE /api/medical-schedules/{scheduleId}
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<String>> deactivate(@PathVariable Long scheduleId) {
        logger.info("DELETE /api/medical-schedules/{} - Desactivar horario", scheduleId);

        medicalScheduleService.deactivate(scheduleId);

        return ResponseEntity
                .ok(ApiResponse.success("Horario desactivado exitosamente"));
    }
}
