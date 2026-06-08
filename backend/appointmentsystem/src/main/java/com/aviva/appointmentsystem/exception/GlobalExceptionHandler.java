package com.aviva.appointmentsystem.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para toda la aplicación.
 * 
 * Usa @RestControllerAdvice (NO @ControllerAdvice) para garantizar
 * que todas las respuestas sean serializadas como JSON automáticamente.
 * 
 * Captura:
 * 1. AppException y subclases → Excepciones de negocio controladas
 * 2. MethodArgumentNotValidException → Errores de validación de DTOs (@Valid)
 * 3. IllegalArgumentException → Errores de argumentos inválidos (ej: enums inválidos)
 * 4. Exception → Catch-all para errores inesperados
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones personalizadas de la aplicación (AppException y subclases).
     * 
     * Incluye: ResourceNotFoundException (404), InvalidCredentialsException (401),
     * UserInactiveException (403), ResourceAlreadyExistsException (409), ValidationException (400)
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        logger.warn("AppException: [{}] {}", ex.getCode(), ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("code", ex.getCode());
        response.put("message", ex.getMessage());
        response.put("status", ex.getStatusCode());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(response);
    }

    /**
     * Maneja errores de validación de entrada (@Valid en DTOs).
     * 
     * Cuando un @RequestBody no pasa las validaciones de jakarta.validation.constraints
     * (@NotBlank, @Email, @Size, etc.), Spring lanza MethodArgumentNotValidException.
     * 
     * Devuelve un mapa de campo -> mensaje de error.
     * Se usa (first, second) -> first para evitar excepción por claves duplicadas.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        logger.warn("Error de validación: {} error(es)", ex.getBindingResult().getErrorCount());

        // Construir mapa de errores campo -> mensaje
        // Se usa merge strategy (first wins) para evitar IllegalStateException por duplicados
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.merge(error.getField(), error.getDefaultMessage(), (first, second) -> first)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("code", "VALIDATION_ERROR");
        response.put("message", "Error en la validación de datos");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Maneja errores de argumentos inválidos.
     * Ej: Gender.valueOf("INVALIDO") lanza IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Argumento inválido: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("code", "INVALID_ARGUMENT");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Catch-all: Maneja cualquier excepción no contemplada.
     * Protege al cliente de stacktraces internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        logger.error("Error inesperado: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("code", "INTERNAL_SERVER_ERROR");
        response.put("message", "Ha ocurrido un error interno del servidor");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
