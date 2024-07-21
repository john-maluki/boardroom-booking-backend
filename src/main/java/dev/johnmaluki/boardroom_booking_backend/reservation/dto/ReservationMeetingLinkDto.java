package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import jakarta.validation.constraints.NotBlank;

public record ReservationMeetingLinkDto(
        @NotBlank(message = "Must not be blank")
        String meetingLink
){}
