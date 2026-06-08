package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear un médico.
 * 
 * Usado en: POST /api/doctors
 * 
 * Validaciones:
 * - firstName, lastName: obligatorios
 * - licenseNumber (colegiatura): obligatoria, entre 5 y 20 caracteres
 * - phone: obligatorio, formato flexible (7-15 caracteres)
 * - email: obligatorio, formato válido
 * - specialtyId: obligatorio (RN-36: un médico DEBE tener especialidad)
 */
public record DoctorRequest(

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    String firstName,

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    String lastName,

    @NotBlank(message = "El número de licencia (colegiatura) no puede estar vacío")
    @Size(min = 5, max = 20, message = "El número de licencia debe tener entre 5 y 20 caracteres")
    String licenseNumber,

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Pattern(regexp = "^[0-9\\-\\+\\s]{7,15}$", message = "El teléfono debe tener entre 7 y 15 caracteres numéricos")
    String phone,

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    String email,

    @NotNull(message = "La especialidad es obligatoria (specialtyId)")
    Long specialtyId

) {
}
