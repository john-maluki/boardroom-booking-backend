package dev.johnmaluki.boardroom_booking_backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserTimezoneDto(
        @NotBlank(message = "Must not be blank")
        String userTimezone
){}
