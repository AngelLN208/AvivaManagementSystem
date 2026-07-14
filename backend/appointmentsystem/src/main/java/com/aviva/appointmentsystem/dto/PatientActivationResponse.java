package com.aviva.appointmentsystem.dto;

import java.util.UUID;

public record PatientActivationResponse(
    PatientActivationStep nextStep,
    UUID challengeId
) {}
