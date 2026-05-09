package com.aviva.appointmentsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay un error de validación
 */
public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(
            message,
            "VALIDATION_ERROR",
            HttpStatus.BAD_REQUEST.value()
        );
    }
}
