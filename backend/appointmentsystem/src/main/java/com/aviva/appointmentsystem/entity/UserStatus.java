package com.aviva.appointmentsystem.entity;

/**
 * Enumeración de estados disponibles para usuarios
 */
public enum UserStatus {
    ACTIVE("Activo", "Usuario activo en el sistema"),
    INACTIVE("Inactivo", "Usuario inactivo"),
    SUSPENDED("Suspendido", "Usuario suspendido"),
    DELETED("Eliminado", "Usuario eliminado");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
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
