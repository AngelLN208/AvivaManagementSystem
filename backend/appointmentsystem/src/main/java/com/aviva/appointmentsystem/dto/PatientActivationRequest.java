package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PatientActivationRequest(
    @NotBlank(message = "El DNI no puede estar vacio")
    @Pattern(
        regexp = "^[0-9]{8,12}$",
        message = "El DNI debe contener entre 8 y 12 digitos"
    )
    String dni
) {}
