package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ReservationEventDateDto(
        @NotNull(message = "The field is required")
        String startDateTime,
        @NotNull(message = "The field is required")
        String endDateTime
) {}
