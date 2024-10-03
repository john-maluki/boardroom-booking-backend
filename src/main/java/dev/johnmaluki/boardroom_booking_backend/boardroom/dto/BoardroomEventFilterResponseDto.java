package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import lombok.Builder;

@Builder
public record BoardroomEventFilterResponseDto(
        Long id,
        String boardroomName,
        String tag
) {}
