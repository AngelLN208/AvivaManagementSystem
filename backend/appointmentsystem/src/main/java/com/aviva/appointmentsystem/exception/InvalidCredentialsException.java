package com.aviva.appointmentsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando las credenciales son inválidas
 */
public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException() {
        super(
            "Usuario o contraseña inválidos",
            "INVALID_CREDENTIALS",
            HttpStatus.UNAUTHORIZED.value()
        );
    }
}
