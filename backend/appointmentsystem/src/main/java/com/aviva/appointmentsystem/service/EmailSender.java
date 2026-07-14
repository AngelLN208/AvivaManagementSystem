package com.aviva.appointmentsystem.service;

public interface EmailSender {

    void send(String recipient, String subject, String message);

    /**
     * Envía una versión HTML conservando texto plano como respaldo para
     * clientes de correo que no renderizan contenido enriquecido.
     *
     * La implementación por defecto mantiene compatibles los adaptadores
     * históricos que solo conocen el contrato de tres argumentos.
     */
    default void send(
            String recipient,
            String subject,
            String textContent,
            String htmlContent
    ) {
        send(recipient, subject, textContent);
    }
}
