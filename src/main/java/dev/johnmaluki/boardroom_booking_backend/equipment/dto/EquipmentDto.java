package dev.johnmaluki.boardroom_booking_backend.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record EquipmentDto(
        @NotBlank(message = "must not be blank")
        String title,
        @NotBlank(message = "must not be blank")
        String description,
        @NotBlank(message = "must not be blank")
        String picture,
        String videoUrl,
        @NotBlank(message = "must not be blank")
        String modelNumber,
        @NotBlank(message = "must not be blank")
        String brand,
        @NotNull
        long boardroomId
) {}
