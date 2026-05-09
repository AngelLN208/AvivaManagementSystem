package com.aviva.appointmentsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando el usuario no es encontrado
 */
public class UserNotFoundException extends AppException {
    public UserNotFoundException(String username) {
        super(
            String.format("Usuario '%s' no encontrado", username),
            "USER_NOT_FOUND",
            HttpStatus.NOT_FOUND.value()
        );
    }
}
