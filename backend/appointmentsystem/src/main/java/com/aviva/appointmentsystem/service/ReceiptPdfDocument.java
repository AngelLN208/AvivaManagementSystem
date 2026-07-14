package com.aviva.appointmentsystem.service;

/** Archivo PDF listo para enviarse como descarga HTTP. */
public record ReceiptPdfDocument(
        String filename,
        byte[] content
) {}
