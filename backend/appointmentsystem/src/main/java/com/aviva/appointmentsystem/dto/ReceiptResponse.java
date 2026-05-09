package com.aviva.appointmentsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un comprobante
 */
public record ReceiptResponse(
    Long id,
    String receiptNumber,
    String description,
    BigDecimal totalAmount,
    LocalDateTime createdAt
) {
}
