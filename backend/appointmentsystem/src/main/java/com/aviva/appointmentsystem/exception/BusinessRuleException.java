package com.aviva.appointmentsystem.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción para violaciones de reglas de negocio del dominio.
 * 
 * HTTP 409 Conflict — la operación es válida en formato pero viola
 * una regla del negocio en el estado actual del sistema.
 * 
 * Casos de uso:
 * - RN-12: Doctor ya tiene una cita activa en ese horario exacto
 * - RN-38: Hora de cita fuera del horario del doctor ese día
 * - RN-14: Intentar reprogramar una cita cancelada o completada
 * - RN-15: Intentar cancelar una cita ya cancelada o completada
 */
public class BusinessRuleException extends AppException {

    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION", HttpStatus.CONFLICT.value());
    }

    public BusinessRuleException(String code, String message) {
        super(message, code, HttpStatus.CONFLICT.value());
    }
}
