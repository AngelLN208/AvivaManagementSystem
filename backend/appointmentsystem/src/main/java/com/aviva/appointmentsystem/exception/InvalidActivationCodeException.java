package com.aviva.appointmentsystem.exception;

import org.springframework.http.HttpStatus;

/** Respuesta deliberadamente generica para no revelar el estado del desafio. */
public class InvalidActivationCodeException extends AppException {

    public InvalidActivationCodeException() {
        super(
            "La verificacion de activacion no es valida o ya vencio",
            "INVALID_ACTIVATION_CODE",
            HttpStatus.BAD_REQUEST.value()
        );
    }
}
