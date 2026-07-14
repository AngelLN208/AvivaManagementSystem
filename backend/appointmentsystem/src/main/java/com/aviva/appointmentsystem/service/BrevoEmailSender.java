package com.aviva.appointmentsystem.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Envia correo transaccional mediante la API HTTPS de Brevo.
 *
 * Se usa HTTP porque los servicios gratuitos de Render bloquean los puertos
 * SMTP. La clave se obtiene de una variable de entorno y nunca se registra.
 */
@Service
public class BrevoEmailSender implements EmailSender {

    private static final String SEND_EMAIL_PATH = "/smtp/email";

    private final RestClient restClient;
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;

    @Autowired
    public BrevoEmailSender(
            @Value("${brevo.api-url}") String apiUrl,
            @Value("${brevo.api-key}") String apiKey,
            @Value("${app.mail.from}") String fromEmail,
            @Value("${app.mail.from-name}") String fromName
    ) {
        this(RestClient.builder(), apiUrl, apiKey, fromEmail, fromName);
    }

    BrevoEmailSender(
            RestClient.Builder restClientBuilder,
            String apiUrl,
            String apiKey,
            String fromEmail,
            String fromName
    ) {
        this.restClient = restClientBuilder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    /** Mantiene el contrato histórico de correo en texto plano. */
    @Override
    public void send(String recipient, String subject, String message) {
        send(recipient, subject, message, null);
    }

    /**
     * Envía contenido multipart alternativo a través de Brevo. Los errores
     * HTTP se propagan para que el scheduler aplique la política de reintentos.
     */
    @Override
    public void send(
            String recipient,
            String subject,
            String textContent,
            String htmlContent
    ) {
        validateConfiguration();
        validateMessage(recipient, subject, textContent);

        BrevoEmailRequest request = new BrevoEmailRequest(
                new Sender(fromName, fromEmail),
                List.of(new Recipient(recipient)),
                subject,
                textContent,
                htmlContent
        );

        restClient.post()
                .uri(SEND_EMAIL_PATH)
                .header("api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("BREVO_API_KEY no esta configurada");
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("MAIL_FROM no esta configurado");
        }
    }

    private void validateMessage(String recipient, String subject, String message) {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("El correo destinatario es obligatorio");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("El asunto del correo es obligatorio");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("El contenido del correo es obligatorio");
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record BrevoEmailRequest(
            Sender sender,
            List<Recipient> to,
            String subject,
            String textContent,
            String htmlContent
    ) {}

    private record Sender(String name, String email) {}

    private record Recipient(String email) {}
}
