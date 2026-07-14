package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record PatientActivationVerifyCodeRequest(
    @NotNull(message = "El identificador de verificacion es requerido")
    UUID challengeId,

    @NotBlank(message = "El codigo de verificacion es requerido")
    @Pattern(regexp = "^[0-9]{6}$", message = "El codigo debe contener 6 digitos")
    String code
) {}
