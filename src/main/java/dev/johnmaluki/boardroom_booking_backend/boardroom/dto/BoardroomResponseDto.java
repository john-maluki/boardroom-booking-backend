package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import lombok.*;

import java.util.List;

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
        List<BoardroomContactResponseDto> boardroomContacts,
        boolean internetEnabled,
        boolean hasOngoingMeeting,
        boolean locked,
        String tag) {}
