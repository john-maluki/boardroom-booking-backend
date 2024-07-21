package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import jakarta.validation.constraints.NotNull;

public record RescheduleReservationDto(
        @NotNull(message = "The field is required")
        String startDate,
        @NotNull(message = "The field is required")
        String endDate,
        @NotNull(message = "The field is required")
        String startTime,
        @NotNull(message = "The field is required")
        String endTime
) {}
