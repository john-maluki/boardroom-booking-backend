package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LockMessageDto(
        @NotBlank(message = "Must not be blank")
        String givenReason
) {}
