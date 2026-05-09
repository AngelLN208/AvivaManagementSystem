package com.aviva.appointmentsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay datos duplicados
 */
public class ResourceAlreadyExistsException extends AppException {
    public ResourceAlreadyExistsException(String message) {
        super(
            message,
            "RESOURCE_ALREADY_EXISTS",
            HttpStatus.CONFLICT.value()
        );
    }
}
