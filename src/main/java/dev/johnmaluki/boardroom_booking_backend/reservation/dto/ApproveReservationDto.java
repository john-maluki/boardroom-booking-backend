package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import jakarta.validation.constraints.NotNull;

public record ApproveReservationDto(
        @NotNull(message = "The field is required")
        ApprovalStatus approvalStatus
) {}
