package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import lombok.Builder;

@Builder
public record ReservationOverlapResponseDto(
        boolean overlap
) {}
