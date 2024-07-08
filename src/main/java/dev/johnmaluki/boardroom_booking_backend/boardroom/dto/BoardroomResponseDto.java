package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import lombok.*;

@Builder
public record BoardroomResponseDto(
        long id,
        String name,
        long capacity,
        String email,
        String centre,
        String department,
        String meetingTypeSupported,
        String picture,
        boolean internetEnabled,
        boolean hasOngoingMeeting,
        boolean locked,
        String tag) {}
