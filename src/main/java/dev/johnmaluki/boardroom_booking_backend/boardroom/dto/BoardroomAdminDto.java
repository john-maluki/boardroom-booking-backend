package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BoardroomAdminDto(
        @NotNull(message = "must not be blank")
        Long userId
){}
