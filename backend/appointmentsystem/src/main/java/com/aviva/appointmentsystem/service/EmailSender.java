package com.aviva.appointmentsystem.service;

public interface EmailSender {

    void send(String recipient, String subject, String message);
}