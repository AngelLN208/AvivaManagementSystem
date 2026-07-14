package com.aviva.appointmentsystem.service;

import com.aviva.appointmentsystem.entity.Appointment;
import com.aviva.appointmentsystem.entity.Doctor;
import com.aviva.appointmentsystem.entity.Patient;
import com.aviva.appointmentsystem.entity.Payment;
import com.aviva.appointmentsystem.entity.PaymentMethod;
import com.aviva.appointmentsystem.entity.Receipt;
import com.aviva.appointmentsystem.entity.Specialty;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptPdfServiceTest {

    @Test
    void generatesReadableSinglePageReceipt() throws Exception {
        ReceiptPdfDocument result = new ReceiptPdfService().generate(sampleReceipt());

        assertEquals("Constancia-RCP-20260714-ABCDEF12.pdf", result.filename());
        assertTrue(result.content().length > 1_000);
        assertTrue(new String(result.content(), 0, 5).startsWith("%PDF-"));

        try (PDDocument document = Loader.loadPDF(result.content())) {
            assertEquals(1, document.getNumberOfPages());
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("RCP-20260714-ABCDEF12"));
            assertTrue(text.contains("María Torres"));
            assertTrue(text.contains("TOTAL REGISTRADO"));
        }

        writePreviewWhenRequested(result.content());
    }

    private void writePreviewWhenRequested(byte[] content) throws Exception {
        String previewPath = System.getProperty("receipt.pdf.preview");
        if (previewPath == null || previewPath.isBlank()) {
            return;
        }

        Path path = Path.of(previewPath).toAbsolutePath().normalize();
        Files.createDirectories(path.getParent());
        Files.write(path, content);
    }

    private Receipt sampleReceipt() {
        Specialty specialty = new Specialty();
        specialty.setName("Cardiología");

        Doctor doctor = new Doctor();
        doctor.setFirstName("Carlos");
        doctor.setLastName("Mendoza");
        doctor.setSpecialty(specialty);

        Patient patient = new Patient();
        patient.setFirstName("María");
        patient.setLastName("Torres");

        Appointment appointment = new Appointment();
        appointment.setId(100L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(LocalDateTime.of(2026, 7, 20, 9, 30));

        Payment payment = new Payment();
        payment.setId(70L);
        payment.setAppointment(appointment);
        payment.setMethod(PaymentMethod.DEBIT_CARD);
        payment.setBaseAmount(new BigDecimal("150.00"));
        payment.setInsuranceCoveredAmount(new BigDecimal("50.00"));
        payment.setAmount(new BigDecimal("100.00"));

        Receipt receipt = new Receipt();
        receipt.setId(9L);
        receipt.setPayment(payment);
        receipt.setReceiptNumber("RCP-20260714-ABCDEF12");
        receipt.setTotalAmount(new BigDecimal("100.00"));
        receipt.setCreatedAt(LocalDateTime.of(2026, 7, 14, 15, 45));
        return receipt;
    }
}
