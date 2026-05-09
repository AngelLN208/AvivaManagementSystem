package com.aviva.appointmentsystem.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO genérico de respuesta API
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    Boolean success,
    String message,
    T data,
    Map<String, String> errors,
    LocalDateTime timestamp
) {

    /**
     * Constructor para respuesta exitosa con datos
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    /**
     * Constructor para respuesta exitosa sin datos
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null, LocalDateTime.now());
    }

    /**
     * Constructor para respuesta con error
     */
    public static <T> ApiResponse<T> error(String message, Map<String, String> errors) {
        return new ApiResponse<>(false, message, null, errors, LocalDateTime.now());
    }
}
