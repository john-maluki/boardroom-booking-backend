package dev.johnmaluki.boardroom_booking_backend.user.dto;

import lombok.Builder;

@Builder
public record SystemAdministratorResponseDto(
        long id,
        String email,
        String tag
) {}
