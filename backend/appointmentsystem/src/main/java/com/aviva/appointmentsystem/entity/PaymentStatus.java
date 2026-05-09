package com.aviva.appointmentsystem.entity;

/**
 * Enumeración de estados disponibles para pagos
 */
public enum PaymentStatus {
    PENDING("Pendiente", "Pago pendiente"),
    PAID("Pagado", "Pago realizado"),
    CANCELLED("Cancelado", "Pago cancelado"),
    REFUNDED("Reembolsado", "Pago reembolsado");

    private final String displayName;
    private final String description;

    PaymentStatus(String displayName, String description) {
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
