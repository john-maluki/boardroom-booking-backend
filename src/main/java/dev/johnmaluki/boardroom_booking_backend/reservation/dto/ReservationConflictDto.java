package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import lombok.Builder;

/**
 * A booking that clashes with a requested time window. Times are ISO-8601 instants in UTC
 * (e.g. {@code 2031-07-01T06:00Z}) so each client can show them in its own time zone.
 */
@Builder
public record ReservationConflictDto(
    long id,
    String meetingTitle,
    String startDateTime,
    String endDateTime,
    ApprovalStatus approvalStatus) {}
