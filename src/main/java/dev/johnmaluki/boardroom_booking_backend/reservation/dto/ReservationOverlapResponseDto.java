package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import java.util.List;
import lombok.Builder;

/** Whether a window is free in a boardroom, and if not, which bookings it clashes with. */
@Builder
public record ReservationOverlapResponseDto(boolean overlap, List<ReservationConflictDto> conflicts) {}
