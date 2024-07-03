package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import lombok.*;

@Builder
public record BoardroomResponseDto(String name, String email, String centre, String department,
                                   String meetingTypeSupported, boolean internetEnabled, boolean locked) {
}
