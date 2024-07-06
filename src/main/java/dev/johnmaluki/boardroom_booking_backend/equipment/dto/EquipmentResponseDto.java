package dev.johnmaluki.boardroom_booking_backend.equipment.dto;


import lombok.Builder;

@Builder
public record EquipmentResponseDto (
        long id,
        String title,
        String description,
        String picture,
        String videoUrl,
        boolean isDisposed,
        String modelNumber,
        String brand,
        long boardroomId,
        String tag

){
}
