package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.Payment;
import com.aviva.appointmentsystem.entity.PaymentMethod;
import com.aviva.appointmentsystem.entity.Receipt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Genera una constancia A4 a partir de datos persistidos del pago.
 *
 * No consulta parámetros enviados por el cliente: identidad, monto, cita y
 * método proceden de la entidad Receipt que ya superó el control de ownership.
 * El documento evita información clínica como motivo, triaje o diagnóstico.
 */
@Service
public class ReceiptPdfService {

    private static final Color TEAL = new Color(7, 95, 88);
    private static final Color TEAL_DARK = new Color(13, 57, 54);
    private static final Color MINT = new Color(239, 249, 247);
    private static final Color BORDER = new Color(214, 230, 226);
    private static final Color TEXT = new Color(27, 48, 46);
    private static final Color MUTED = new Color(94, 116, 112);
    private static final Color WHITE = Color.WHITE;

    private static final Locale SPANISH_LOCALE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm", SPANISH_LOCALE);
    private static final DateTimeFormatter APPOINTMENT_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm", SPANISH_LOCALE);

    /** Devuelve bytes autocontenidos; no crea archivos temporales en servidor. */
    public ReceiptPdfDocument generate(Receipt receipt) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            configureMetadata(document, receipt);
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream canvas = new PDPageContentStream(document, page)) {
                PdfFonts fonts = new PdfFonts(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                );
                drawReceipt(canvas, receipt, fonts);
            }

            document.save(output);
            return new ReceiptPdfDocument(
                    filename(receipt.getReceiptNumber()),
                    output.toByteArray()
            );
        } catch (IOException exception) {
            throw new UncheckedIOException("No se pudo generar la constancia PDF", exception);
        }
    }

    private void drawReceipt(
            PDPageContentStream canvas,
            Receipt receipt,
            PdfFonts fonts
    )
            throws IOException {
        Payment payment = receipt.getPayment();
        Appointment appointment = payment != null ? payment.getAppointment() : null;
        Patient patient = appointment != null ? appointment.getPatient() : null;
        Doctor doctor = appointment != null ? appointment.getDoctor() : null;

        drawHeader(canvas, fonts);

        drawText(canvas, fonts.bold(), 10, TEAL, 48, 710, "CONSTANCIA DE PAGO");
        drawText(canvas, fonts.bold(), 20, TEAL_DARK, 48, 680,
                safe(receipt.getReceiptNumber(), "Constancia Aviva"));
        drawText(canvas, fonts.regular(), 10, MUTED, 48, 658,
                "Emitida el " + format(receipt.getCreatedAt(), DATE_TIME_FORMATTER));

        drawBox(canvas, 48, 564, 499, 70, MINT, BORDER);
        drawLabelValue(canvas, fonts, 66, 610, "PACIENTE", patientName(patient), 220);
        drawLabelValue(canvas, fonts, 310, 610, "FECHA DE LA CITA",
                appointment != null
                        ? format(appointment.getAppointmentDateTime(), APPOINTMENT_FORMATTER)
                        : "No disponible",
                215);

        drawText(canvas, fonts.bold(), 11, TEAL_DARK, 48, 531, "Detalle de atención");
        drawLabelValue(canvas, fonts, 48, 505, "PROFESIONAL", doctorName(doctor), 230);
        drawLabelValue(canvas, fonts, 310, 505, "ESPECIALIDAD", specialtyName(doctor), 215);
        drawLabelValue(canvas, fonts, 48, 457, "PAGO", idLabel(payment != null ? payment.getId() : null), 230);
        drawLabelValue(canvas, fonts, 310, 457, "CITA", idLabel(appointment != null ? appointment.getId() : null), 215);

        drawText(canvas, fonts.bold(), 11, TEAL_DARK, 48, 409, "Resumen del pago");
        drawBox(canvas, 48, 318, 499, 70, MINT, BORDER);
        drawAmount(canvas, fonts, 66, 362, "VALOR DE LA ATENCIÓN", baseAmount(payment, receipt));
        drawAmount(canvas, fonts, 235, 362, "COBERTURA DEL SEGURO", coveredAmount(payment));
        drawAmount(canvas, fonts, 405, 362, "TOTAL REGISTRADO", receipt.getTotalAmount());

        drawLabelValue(canvas, fonts, 48, 284, "MÉTODO DE PAGO",
                paymentMethod(payment != null ? payment.getMethod() : null), 230);
        drawLabelValue(canvas, fonts, 310, 284, "ESTADO", "Pago confirmado", 215);

        drawText(canvas, fonts.bold(), 11, TEAL_DARK, 48, 229, "Descripción");
        drawWrappedText(
                canvas,
                fonts.regular(),
                10.5f,
                TEXT,
                48,
                207,
                499,
                15,
                "Constancia correspondiente al pago de una cita médica en Clínica Aviva."
        );

        canvas.setStrokingColor(BORDER);
        canvas.setLineWidth(0.8f);
        canvas.moveTo(48, 92);
        canvas.lineTo(547, 92);
        canvas.stroke();
        drawText(canvas, fonts.regular(), 8.5f, MUTED, 48, 72,
                "Documento emitido desde el portal de pacientes de Clínica Aviva.");
        drawText(canvas, fonts.regular(), 8.5f, MUTED, 493, 72, "Página 1 de 1");
    }

    private void drawHeader(PDPageContentStream canvas, PdfFonts fonts) throws IOException {
        canvas.setNonStrokingColor(TEAL);
        canvas.addRect(0, 742, PDRectangle.A4.getWidth(), 100);
        canvas.fill();

        drawText(canvas, fonts.bold(), 25, WHITE, 48, 788, "AVIVA");
        drawText(canvas, fonts.regular(), 10, new Color(215, 244, 238), 48, 769,
                "Clínica Aviva");
        drawText(canvas, fonts.bold(), 8.5f, new Color(215, 244, 238), 425, 783,
                "PORTAL DE PACIENTES");
    }

    private void drawAmount(
            PDPageContentStream canvas,
            PdfFonts fonts,
            float x,
            float y,
            String label,
            BigDecimal amount
    ) throws IOException {
        drawText(canvas, fonts.bold(), 7.2f, MUTED, x, y, label);
        drawText(canvas, fonts.bold(), 14, TEAL_DARK, x, y - 23, currency(amount));
    }

    private void drawLabelValue(
            PDPageContentStream canvas,
            PdfFonts fonts,
            float x,
            float y,
            String label,
            String value,
            float maxWidth
    ) throws IOException {
        drawText(canvas, fonts.bold(), 7.5f, MUTED, x, y, label);
        drawText(canvas, fonts.bold(), 10.5f, TEXT, x, y - 20,
                fitText(fonts.bold(), 10.5f, value, maxWidth));
    }

    private void drawBox(
            PDPageContentStream canvas,
            float x,
            float y,
            float width,
            float height,
            Color fill,
            Color stroke
    ) throws IOException {
        canvas.setNonStrokingColor(fill);
        canvas.addRect(x, y, width, height);
        canvas.fill();
        canvas.setStrokingColor(stroke);
        canvas.setLineWidth(0.8f);
        canvas.addRect(x, y, width, height);
        canvas.stroke();
    }

    private void drawWrappedText(
            PDPageContentStream canvas,
            PDType1Font font,
            float fontSize,
            Color color,
            float x,
            float y,
            float maxWidth,
            float leading,
            String value
    ) throws IOException {
        float currentY = y;
        for (String line : wrap(font, fontSize, clean(value), maxWidth)) {
            drawText(canvas, font, fontSize, color, x, currentY, line);
            currentY -= leading;
        }
    }

    private void drawText(
            PDPageContentStream canvas,
            PDType1Font font,
            float fontSize,
            Color color,
            float x,
            float y,
            String value
    ) throws IOException {
        canvas.beginText();
        canvas.setFont(font, fontSize);
        canvas.setNonStrokingColor(color);
        canvas.newLineAtOffset(x, y);
        canvas.showText(clean(value));
        canvas.endText();
    }

    private List<String> wrap(
            PDType1Font font,
            float fontSize,
            String value,
            float maxWidth
    ) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : value.split("\\s+")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (textWidth(font, fontSize, candidate) <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
            } else {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                }
                current.setLength(0);
                current.append(word);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private String fitText(
            PDType1Font font,
            float fontSize,
            String value,
            float maxWidth
    ) throws IOException {
        String cleaned = clean(value);
        if (textWidth(font, fontSize, cleaned) <= maxWidth) {
            return cleaned;
        }

        String suffix = "...";
        String shortened = cleaned;
        while (!shortened.isEmpty()
                && textWidth(font, fontSize, shortened + suffix) > maxWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened.stripTrailing() + suffix;
    }

    private float textWidth(PDType1Font font, float size, String text)
            throws IOException {
        return font.getStringWidth(clean(text)) / 1000f * size;
    }

    private void configureMetadata(PDDocument document, Receipt receipt) {
        PDDocumentInformation information = new PDDocumentInformation();
        information.setTitle("Constancia de pago "
                + safe(receipt.getReceiptNumber(), "Aviva"));
        information.setAuthor("Clínica Aviva");
        information.setSubject("Constancia de pago del portal de pacientes");
        information.setCreator("Aviva Management System");
        document.setDocumentInformation(information);
    }

    private String patientName(Patient patient) {
        return patient == null
                ? "Paciente Aviva"
                : safe(patient.getFirstName(), "") + " " + safe(patient.getLastName(), "");
    }

    private String doctorName(Doctor doctor) {
        return doctor == null
                ? "Equipo médico Aviva"
                : "Dr. " + safe(doctor.getFirstName(), "") + " "
                + safe(doctor.getLastName(), "");
    }

    private String specialtyName(Doctor doctor) {
        return doctor != null && doctor.getSpecialty() != null
                ? safe(doctor.getSpecialty().getName(), "Atención médica")
                : "Atención médica";
    }

    private BigDecimal baseAmount(Payment payment, Receipt receipt) {
        if (payment != null && payment.getBaseAmount() != null) {
            return payment.getBaseAmount();
        }
        return receipt.getTotalAmount();
    }

    private BigDecimal coveredAmount(Payment payment) {
        return payment != null && payment.getInsuranceCoveredAmount() != null
                ? payment.getInsuranceCoveredAmount()
                : BigDecimal.ZERO;
    }

    private String paymentMethod(PaymentMethod method) {
        if (method == null) {
            return "No disponible";
        }
        return switch (method) {
            case CASH -> "Efectivo";
            case CREDIT_CARD -> "Tarjeta de crédito";
            case DEBIT_CARD -> "Tarjeta de débito";
            case TRANSFER -> "Transferencia";
            case INSURANCE -> "Cobertura de seguro";
        };
    }

    private String currency(BigDecimal amount) {
        BigDecimal safeAmount = amount != null ? amount : BigDecimal.ZERO;
        return "S/ " + safeAmount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String format(LocalDateTime value, DateTimeFormatter formatter) {
        return value != null ? value.format(formatter) : "No disponible";
    }

    private String idLabel(Long id) {
        return id != null ? "#" + id : "No disponible";
    }

    private String filename(String receiptNumber) {
        String normalized = safe(receiptNumber, "constancia-aviva")
                .replaceAll("[^A-Za-z0-9_-]", "-");
        return "Constancia-" + normalized + ".pdf";
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    /** Normaliza signos que las fuentes PDF estándar no representan. */
    private String clean(String value) {
        return safe(value, "")
                .replace('\u2013', '-')
                .replace('\u2014', '-')
                .replace('\u2026', '.')
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "");
    }

    /** Las fuentes se crean por documento para soportar descargas concurrentes. */
    private record PdfFonts(PDType1Font regular, PDType1Font bold) {}
}
