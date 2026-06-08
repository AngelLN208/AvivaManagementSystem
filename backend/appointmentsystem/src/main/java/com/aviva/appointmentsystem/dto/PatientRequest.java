package com.aviva.appointmentsystem.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar un paciente (uso interno del personal).
 * 
 * Usado en:
 * - POST /api/patients (crear paciente — uso interno)
 * - PUT /api/patients/{id} (actualizar paciente)
 * 
 * NOTA: Para auto-registro de pacientes desde el portal web, 
 * se usa RegisterPatientRequest (que incluye username y password).
 * 
 * Validaciones de la UTP (Semana 11):
 * - DNI: exactamente 8 dígitos numéricos (@Pattern)
 * - email: formato válido (@Email)
 * - dateOfBirth: fecha en el pasado (@Past)
 * - firstName, lastName: obligatorios (@NotBlank)
 */
public record PatientRequest(

    @NotBlank(message = "El DNI no puede estar vacío")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe contener exactamente 8 dígitos numéricos")
    String dni,

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    String firstName,

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    String lastName,

    @NotBlank(message = "El género no puede estar vacío")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "El género debe ser: MALE, FEMALE u OTHER")
    String gender,

    @NotNull(message = "La fecha de nacimiento no puede ser nula")
    @Past(message = "La fecha de nacimiento debe ser una fecha en el pasado")
    LocalDate dateOfBirth,

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Pattern(regexp = "^[0-9\\-\\+\\s]{7,15}$", message = "El teléfono debe tener entre 7 y 15 caracteres numéricos")
    String phone,

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe tener un formato válido")
    String email,

    String address

) {
}
