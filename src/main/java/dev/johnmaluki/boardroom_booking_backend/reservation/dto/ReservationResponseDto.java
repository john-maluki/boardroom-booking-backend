package dev.johnmaluki.boardroom_booking_backend.reservation.dto;

import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import dev.johnmaluki.boardroom_booking_backend.util.MeetingType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record ReservationResponseDto(long id, String meetingTitle, String meetingDescription, MeetingType meetingType,
                                     String attendees, long boardroomId, ApprovalStatus approvalStatus,
                                     LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, String tag) {
}
