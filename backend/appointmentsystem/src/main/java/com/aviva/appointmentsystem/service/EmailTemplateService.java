package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.Notification;
import com.aviva.appointmentsystem.entity.Patient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renderiza los correos transaccionales desde archivos HTML del backend.
 *
 * Los servicios de dominio solo entregan el evento y sus datos. La identidad
 * visual queda centralizada aquí y todos los valores dinámicos se escapan antes
 * de insertarse para evitar que información del usuario se interprete como HTML.
 */
@Service
public class EmailTemplateService {

    private static final String TEMPLATE_ROOT = "templates/email/";
    private static final Locale SPANISH_LOCALE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", SPANISH_LOCALE);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", SPANISH_LOCALE);
    private static final Pattern RECEIPT_PATTERN =
            Pattern.compile("\\bRCP-[A-Z0-9-]+\\b", Pattern.CASE_INSENSITIVE);

    private final String portalUrl;
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    public EmailTemplateService(
            @Value("${app.portal.url:http://localhost:5174}") String portalUrl
    ) {
        this.portalUrl = stripTrailingSlash(portalUrl);
    }

    /** Construye el correo OTP sin exponer su contenido en el service de activación. */
    public EmailContent patientActivation(
            Patient patient,
            String otp,
            long expirationMinutes
    ) {
        String subject = "Código para activar tu cuenta en Clínica Aviva";
        String text = "Tu código de activación es: " + otp
                + System.lineSeparator()
                + "Vence en " + expirationMinutes + " minutos. "
                + "Si no solicitaste este código, puedes ignorar este mensaje.";

        Map<String, String> values = commonValues(
                patient.getFirstName(),
                subject,
                "Activa tu cuenta de paciente"
        );
        values.put("code", otp);
        values.put("expirationMinutes", String.valueOf(expirationMinutes));
        values.put("actionUrl", portalUrl + "/registro");

        return new EmailContent(
                subject,
                text,
                renderEmail("patient-activation.html", values)
        );
    }

    /**
     * Construye el correo de una notificación persistida. El texto corto
     * almacenado sigue siendo la versión IN_APP y el respaldo de texto plano.
     */
    public EmailContent notification(Notification notification) {
        Map<String, String> values = appointmentValues(notification);
        String fragment = switch (notification.getType()) {
            case PAYMENT_RECEIVED -> {
                values.put("receiptNumber", receiptNumber(notification.getMessage()));
                values.put("actionUrl", portalUrl + "/pagos");
                yield "payment-confirmed.html";
            }
            case APPOINTMENT_CREATED,
                 APPOINTMENT_RESCHEDULED,
                 APPOINTMENT_CANCELLED,
                 APPOINTMENT_REMINDER,
                 APPOINTMENT_CONFIRMED,
                 APPOINTMENT_UPDATED -> {
                values.put("actionUrl", portalUrl + "/citas");
                yield "appointment-event.html";
            }
            default -> {
                values.put("actionUrl", portalUrl);
                yield "generic-notification.html";
            }
        };

        return new EmailContent(
                notification.getSubject(),
                notification.getMessage(),
                renderEmail(fragment, values)
        );
    }

    private Map<String, String> appointmentValues(Notification notification) {
        Map<String, String> values = commonValues(
                notification.getRecipientName(),
                notification.getSubject(),
                notification.getSubject()
        );
        values.put("message", valueOrFallback(notification.getMessage(), "Tienes una nueva notificación."));
        values.put("eventLabel", eventLabel(notification.getType()));

        Appointment appointment = notification.getAppointment();
        if (appointment == null) {
            values.put("appointmentId", "—");
            values.put("appointmentDate", "Fecha por confirmar");
            values.put("appointmentTime", "—");
            values.put("doctorName", "Equipo médico Aviva");
            values.put("specialty", "Atención médica");
            return values;
        }

        values.put("appointmentId", String.valueOf(appointment.getId()));
        LocalDateTime dateTime = appointment.getAppointmentDateTime();
        values.put("appointmentDate", dateTime != null
                ? capitalize(dateTime.format(DATE_FORMATTER))
                : "Fecha por confirmar");
        values.put("appointmentTime", dateTime != null
                ? dateTime.format(TIME_FORMATTER)
                : "—");

        Doctor doctor = appointment.getDoctor();
        values.put("doctorName", doctor != null
                ? "Dr. " + doctor.getFirstName() + " " + doctor.getLastName()
                : "Equipo médico Aviva");
        values.put("specialty", doctor != null && doctor.getSpecialty() != null
                ? doctor.getSpecialty().getName()
                : "Atención médica");
        return values;
    }

    private Map<String, String> commonValues(
            String recipientName,
            String preheader,
            String heading
    ) {
        Map<String, String> values = new HashMap<>();
        values.put("recipientName", valueOrFallback(recipientName, "paciente"));
        values.put("preheader", valueOrFallback(preheader, "Clínica Aviva"));
        values.put("heading", valueOrFallback(heading, "Clínica Aviva"));
        values.put("portalUrl", portalUrl);
        return values;
    }

    private String renderEmail(String fragmentName, Map<String, String> values) {
        String fragment = replaceEscaped(loadTemplate(fragmentName), values);
        String layout = replaceEscaped(loadTemplate("layout.html"), values);
        // El fragmento ya contiene únicamente HTML controlado y valores escapados.
        return layout.replace("{{content}}", fragment);
    }

    private String replaceEscaped(String template, Map<String, String> values) {
        String rendered = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            rendered = rendered.replace(
                    "{{" + entry.getKey() + "}}",
                    escapeHtml(entry.getValue())
            );
        }
        return rendered;
    }

    private String loadTemplate(String name) {
        return templateCache.computeIfAbsent(name, this::readTemplate);
    }

    private String readTemplate(String name) {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_ROOT + name);
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "No se pudo cargar la plantilla de correo " + name,
                    exception
            );
        }
    }

    private String receiptNumber(String message) {
        Matcher matcher = RECEIPT_PATTERN.matcher(valueOrFallback(message, ""));
        return matcher.find() ? matcher.group().toUpperCase(SPANISH_LOCALE) : "Disponible en tu portal";
    }

    private String eventLabel(Notification.NotificationType type) {
        return switch (type) {
            case APPOINTMENT_CREATED -> "Nueva cita";
            case APPOINTMENT_RESCHEDULED -> "Cita reprogramada";
            case APPOINTMENT_CANCELLED -> "Cita cancelada";
            case APPOINTMENT_REMINDER -> "Recordatorio";
            case APPOINTMENT_CONFIRMED -> "Cita confirmada";
            case PAYMENT_RECEIVED -> "Pago confirmado";
            default -> "Actualización";
        };
    }

    private String escapeHtml(String value) {
        return valueOrFallback(value, "")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String capitalize(String value) {
        return value == null || value.isEmpty()
                ? value
                : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:5174";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
