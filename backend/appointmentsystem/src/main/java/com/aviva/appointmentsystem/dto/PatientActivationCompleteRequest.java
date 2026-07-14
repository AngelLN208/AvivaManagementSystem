package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PatientActivationCompleteRequest(
    @NotNull(message = "El identificador de verificacion es requerido")
    UUID challengeId,

    @NotBlank(message = "El token de activacion es requerido")
    @Size(min = 32, max = 200, message = "El token de activacion no es valido")
    String activationToken,

    @NotBlank(message = "El nombre de usuario no puede estar vacio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Pattern(
        regexp = "^[A-Za-z0-9._-]+$",
        message = "El nombre de usuario solo puede contener letras, numeros, punto, guion y guion bajo"
    )
    String username,

    @NotBlank(message = "La contrasena no puede estar vacia")
    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    String password
) {}
