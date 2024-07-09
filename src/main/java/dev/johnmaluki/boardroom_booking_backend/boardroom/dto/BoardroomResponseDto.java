package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import lombok.*;

@Builder
public record BoardroomResponseDto(
        Long id,
        String name,
        int capacity,
        String email,
        String centre,
        String description,
        String department,
        String meetingTypeSupported,
        String picture,
        boolean internetEnabled,
        boolean hasOngoingMeeting,
        boolean locked,
        String tag) {}
