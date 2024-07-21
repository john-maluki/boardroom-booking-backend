package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeVenueDto(
        @NotNull(message = "This field is required")
        Long boardroomId
) {}
