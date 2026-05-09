package com.aviva.appointmentsystem.entity;

/**
 * Enumeración de roles disponibles en el sistema
 */
public enum Role {
    ADMIN("Administrador", "Admin del sistema"),
    DOCTOR("Doctor", "Médico del sistema"),
    RECEPTIONIST("Recepcionista", "Personal de recepción"),
    PATIENT("Paciente", "Paciente del sistema");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
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
