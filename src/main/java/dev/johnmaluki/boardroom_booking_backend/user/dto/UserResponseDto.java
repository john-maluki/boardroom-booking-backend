package dev.johnmaluki.boardroom_booking_backend.user.dto;

import lombok.Builder;

@Builder
public record UserResponseDto(
       long id,
       String username,
       String department,
       String email,
       String fullName,
       String timeZone,
       String tag
) {}
