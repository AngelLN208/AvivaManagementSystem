package com.aviva.appointmentsystem.entity;

/**
 * Enumeración de métodos de pago disponibles
 */
public enum PaymentMethod {
    CASH("Efectivo", "Pago en efectivo"),
    CREDIT_CARD("Tarjeta de Crédito", "Pago con tarjeta de crédito"),
    DEBIT_CARD("Tarjeta de Débito", "Pago con tarjeta de débito"),
    TRANSFER("Transferencia", "Pago por transferencia bancaria"),
    INSURANCE("Seguro", "Pago por seguro médico");

    private final String displayName;
    private final String description;

    PaymentMethod(String displayName, String description) {
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
