package com.aviva.appointmentsystem.dto;

import java.util.UUID;

/**
 * El token se entrega una sola vez y solo sirve para completar este challenge.
 */
public record PatientActivationVerifyCodeResponse(
    UUID challengeId,
    String activationToken,
    long expiresInSeconds
) {}
