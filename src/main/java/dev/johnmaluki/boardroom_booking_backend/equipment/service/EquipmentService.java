package dev.johnmaluki.boardroom_booking_backend.equipment.service;

import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;

import java.util.List;

public interface EquipmentService {
    List<EquipmentResponseDto> getAllEquipments();
    EquipmentResponseDto createEquipment(EquipmentDto equipmentDto);
    void removeEquipment(long equipmentId);
    EquipmentResponseDto updateEquipment(long equipmentId, EquipmentDto equipmentDto);
}
