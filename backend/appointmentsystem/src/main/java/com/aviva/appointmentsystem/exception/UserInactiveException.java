package com.aviva.appointmentsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando el usuario está inactivo
 */
public class UserInactiveException extends AppException {
    public UserInactiveException(String username) {
        super(
            String.format("Usuario '%s' está inactivo", username),
            "USER_INACTIVE",
            HttpStatus.FORBIDDEN.value()
        );
    }
}
