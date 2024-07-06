package dev.johnmaluki.boardroom_booking_backend.equipment.service;

import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;

import java.util.List;

public interface EquipmentService {
    List<EquipmentResponseDto> getAllEquipments();
    List<EquipmentResponseDto> getBoardroomEquipments(long boardroomId);
}
