package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import lombok.*;

@Builder
public record BoardroomResponseDto(long id, String name, String email, String centre, String department,
                                   String meetingTypeSupported, String picture, boolean internetEnabled, boolean locked, String tag) {
}
