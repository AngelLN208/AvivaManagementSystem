package com.aviva.appointmentsystem.dto;

/**
 * DTO para la respuesta de login
 */
public record LoginResponse(
    String token,
    String role,
    String username,
    String firstName,
    String lastName
) {
}
