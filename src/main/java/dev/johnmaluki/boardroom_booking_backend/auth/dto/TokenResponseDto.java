package dev.johnmaluki.boardroom_booking_backend.auth.dto;

import lombok.Builder;

/** Field names match what the previous servlet filter wrote, so clients need no change. */
@Builder
public record TokenResponseDto(String accessToken, String refreshToken) {}
