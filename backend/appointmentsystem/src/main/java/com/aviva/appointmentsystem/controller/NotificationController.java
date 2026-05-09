package com.aviva.appointmentsystem.controller;

import com.aviva.appointmentsystem.dto.ApiResponse;
import com.aviva.appointmentsystem.dto.NotificationResponse;
import com.aviva.appointmentsystem.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    /**
     * Obtiene notificaciones de un usuario
     * GET /api/notifications/user?email=...
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            @RequestParam String email) {
        logger.info("GET /api/notifications/user - Obtener notificaciones del usuario");

        List<NotificationResponse> response = notificationService.getUserNotifications(email);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Notificaciones obtenidas: " + response.size()));
    }

    /**
     * Obtiene notificaciones de una cita
     * GET /api/notifications/appointment/{appointmentId}
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAppointmentNotifications(
            @PathVariable Long appointmentId) {
        logger.info("GET /api/notifications/appointment/{} - Obtener notificaciones de cita", appointmentId);

        List<NotificationResponse> response = notificationService.getAppointmentNotifications(appointmentId);

        return ResponseEntity
                .ok(ApiResponse.success(response, "Notificaciones obtenidas: " + response.size()));
    }
}
