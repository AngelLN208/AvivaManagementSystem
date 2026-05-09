package com.aviva.appointmentsystem.entity;

/**
 * Enumeración de estados disponibles para citas médicas
 */
public enum AppointmentStatus {
    PENDING("Pendiente", "Cita pendiente de confirmación"),
    CONFIRMED("Confirmada", "Cita confirmada"),
    COMPLETED("Completada", "Cita completada"),
    CANCELLED("Cancelada", "Cita cancelada"),
    RESCHEDULED("Reprogramada", "Cita reprogramada"),
    NO_SHOW("No se presentó", "Paciente no se presentó");

    private final String displayName;
    private final String description;

    AppointmentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
