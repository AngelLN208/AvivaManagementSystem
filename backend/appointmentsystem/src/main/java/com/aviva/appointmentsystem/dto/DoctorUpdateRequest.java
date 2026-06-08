package com.aviva.appointmentsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualización parcial de un médico.
 * 
 * Usado en: PUT /api/doctors/{id}
 * 
 * Todos los campos son opcionales (nullable).
 * El Service aplicará solo los campos presentes (no nulos).
 * 
 * Ejemplo del Postman: { "phone": "999333555" }
 * → Solo se actualiza el teléfono, el resto queda igual.
 */
public record DoctorUpdateRequest(

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    String firstName,

    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    String lastName,

    @Size(min = 5, max = 20, message = "El número de licencia debe tener entre 5 y 20 caracteres")
    String licenseNumber,

    @Pattern(regexp = "^[0-9\\-\\+\\s]{7,15}$", message = "El teléfono debe tener entre 7 y 15 caracteres numéricos")
    String phone,

    @Email(message = "El email debe ser válido")
    String email,

    Long specialtyId

) {
}
