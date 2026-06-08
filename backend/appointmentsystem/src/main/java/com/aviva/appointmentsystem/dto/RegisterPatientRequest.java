package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para el auto-registro de pacientes desde el portal web.
 * 
 * POST /api/auth/register-patient
 * 
 * A diferencia de PatientRequest (uso interno), este DTO incluye
 * los campos username y password necesarios para crear el User asociado.
 * 
 * Flujo: Controller recibe este DTO → Service crea User + Patient → Devuelve PatientResponse
 */
public record RegisterPatientRequest(

    // ── Campos de autenticación (User) ──

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    String username,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    String password,

    // ── Campos del paciente (Patient) ──

    @NotBlank(message = "El DNI no puede estar vacío")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "El DNI debe contener entre 8 y 12 dígitos")
    String dni,

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    String firstName,

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    String lastName,

    @NotBlank(message = "El género no puede estar vacío")
    String gender,

    @NotBlank(message = "La fecha de nacimiento no puede estar vacía")
    String dateOfBirth,

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Pattern(regexp = "^[0-9\\-\\+\\s]{7,15}$", message = "El teléfono debe tener entre 7 y 15 caracteres numéricos")
    String phone,

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    String email,

    String address

) {
}
