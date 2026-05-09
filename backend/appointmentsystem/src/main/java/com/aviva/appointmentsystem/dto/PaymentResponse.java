package com.aviva.appointmentsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un pago
 */
public record PaymentResponse(
    Long id,
    BigDecimal amount,
    String status,
    String method,
    String description,
    LocalDateTime paymentDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
