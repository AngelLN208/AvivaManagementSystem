package com.aviva.appointmentsystem.exception;

import org.springframework.http.HttpStatus;

/** Limite temporal para evitar spam de codigos al correo del paciente. */
public class ActivationRequestLimitedException extends AppException {

    public ActivationRequestLimitedException() {
        super(
            "Espera antes de solicitar un nuevo codigo de activacion",
            "ACTIVATION_REQUEST_LIMITED",
            HttpStatus.TOO_MANY_REQUESTS.value()
        );
    }
}
