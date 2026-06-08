package com.aviva.appointmentsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta enriquecido para un comprobante de pago.
 *
 * Incluye paymentId y appointmentId para que el frontend pueda
 * navegar al pago y la cita asociada sin peticiones adicionales.
 */
public record ReceiptResponse(
    Long id,
    String receiptNumber,
    String description,
    BigDecimal totalAmount,
    Long paymentId,
    Long appointmentId,
    LocalDateTime createdAt
) {
}
