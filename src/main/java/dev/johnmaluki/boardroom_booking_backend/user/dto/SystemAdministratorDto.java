package dev.johnmaluki.boardroom_booking_backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SystemAdministratorDto (
        @NotBlank(message = "Must not be empty")
                @Email(message = "Invalid email")
       String email
){}
