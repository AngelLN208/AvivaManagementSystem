package com.aviva.appointmentsystem.dto;

import java.time.LocalTime;

public record AvailableSlotResponse(
    LocalTime startTime,
    LocalTime endTime
) {}