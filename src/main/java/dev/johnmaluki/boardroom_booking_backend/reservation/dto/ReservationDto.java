package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import dev.johnmaluki.boardroom_booking_backend.util.MeetingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ReservationDto(
        @NotBlank(message = "Must not be blank")
        String meetingTitle,
        @NotBlank(message = "Must not be blank")
        String meetingDescription,
        @NotNull(message = "The field is required")
        MeetingType meetingType,
        @NotBlank(message = "Must not be blank")
        String attendees,
        @NotNull(message = "The field is required")
        long boardroomId,
        boolean ictSupportRequired,
        boolean isUrgentMeeting,
        boolean recordMeeting,
        @NotNull(message = "The field is required")
        String startDateTime,
        @NotNull(message = "The field is required")
        String endDateTime
) {}
