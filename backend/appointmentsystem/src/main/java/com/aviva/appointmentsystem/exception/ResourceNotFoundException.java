package com.aviva.appointmentsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando no se encuentra un recurso
 */
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) {
        super(
            message,
            "RESOURCE_NOT_FOUND",
            HttpStatus.NOT_FOUND.value()
        );
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(
            String.format("%s con ID %d no encontrado", resourceName, id),
            "RESOURCE_NOT_FOUND",
            HttpStatus.NOT_FOUND.value()
        );
    }
}
