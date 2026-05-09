package com.aviva.appointmentsystem.exception;

/**
 * Excepción base para todas las excepciones personalizadas del sistema
 */
public class AppException extends RuntimeException {
    private final String code;
    private final int statusCode;

    public AppException(String message, String code, int statusCode) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
