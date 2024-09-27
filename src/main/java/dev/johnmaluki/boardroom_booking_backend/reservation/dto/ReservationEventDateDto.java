package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationEventDateDto(
        @NotNull(message = "The field is required")
        String startDateTime,
        @NotNull(message = "The field is required")
        String endDateTime
) {}
