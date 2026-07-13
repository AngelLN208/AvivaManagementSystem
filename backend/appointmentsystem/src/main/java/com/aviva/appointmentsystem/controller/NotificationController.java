package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.NotificationResponse;
import com.aviva.appointmentsystem.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de notificaciones
 * Gestiona notificaciones del sistema (RF-45 a RF-48)
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notificaciones", description = "Consulta y lectura de notificaciones del paciente")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Obtiene notificaciones de un usuario
     * GET /api/notifications/user?email=...
     */
    @GetMapping("/user")
    @Operation(summary = "Listar todas las notificaciones de un usuario")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            @RequestParam String email) {
        logger.info("GET /api/notifications/user - Obtener notificaciones del usuario");

        List<NotificationResponse> response = notificationService.getUserNotifications(email);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Notificaciones obtenidas: " + response.size()));
    }

    /**
     * Lista las notificaciones internas que debe mostrar el portal paciente.
     */
    @GetMapping("/user/in-app")
    @Operation(summary = "Listar notificaciones internas del portal paciente")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserInAppNotifications(
            @RequestParam String email) {
        List<NotificationResponse> response =
                notificationService.getUserInAppNotifications(email);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Notificaciones internas obtenidas: " + response.size())
        );
    }

    /**
     * Registra la primera lectura de una notificacion interna.
     */
    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Marcar una notificacion interna como leida")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long notificationId) {
        NotificationResponse response = notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Notificacion marcada como leida")
        );
    }

    /**
     * Obtiene notificaciones de una cita
     * GET /api/notifications/appointment/{appointmentId}
     */
    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Listar notificaciones relacionadas con una cita")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAppointmentNotifications(
            @PathVariable Long appointmentId) {
        logger.info("GET /api/notifications/appointment/{} - Obtener notificaciones de cita", appointmentId);

        List<NotificationResponse> response = notificationService.getAppointmentNotifications(appointmentId);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Notificaciones obtenidas: " + response.size()));
    }
}
