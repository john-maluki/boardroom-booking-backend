package dev.johnmaluki.boardroom_booking_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RefreshTokenRequestDto(
    @NotBlank(message = "Must not be blank") String refreshToken) {}
