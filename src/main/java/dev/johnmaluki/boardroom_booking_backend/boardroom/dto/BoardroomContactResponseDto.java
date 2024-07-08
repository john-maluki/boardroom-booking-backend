package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import lombok.Builder;

@Builder
public record BoardroomContactResponseDto(
        long id,
        String contact,
        String tag
) {}
