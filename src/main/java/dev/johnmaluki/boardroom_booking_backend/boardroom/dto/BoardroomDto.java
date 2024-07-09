package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BoardroomDto(
        @NotBlank(message = "must not be blank")
        String name,
        @Min(2)
        int capacity,
        @Email(message = "must be a valid email")
        String email,
        @NotBlank(message = "must not be blank")
        String centre,
        @NotBlank(message = "must not be blank")
        String description,
        @NotBlank(message = "must not be blank")
        String department,
        @NotBlank(message = "must not be blank")
        String meetingTypeSupported,
        @NotBlank(message = "must not be blank")
        String picture,
        @NotNull(message = "must not be null")
        boolean internetEnabled
){}
