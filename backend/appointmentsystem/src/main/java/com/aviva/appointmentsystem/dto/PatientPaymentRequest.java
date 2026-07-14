package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos necesarios para registrar el pago propio desde el portal. */
public record PatientPaymentRequest(
        @NotBlank(message = "El método de pago es requerido")
        String method
) {
}
