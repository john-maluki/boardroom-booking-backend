package dev.johnmaluki.boardroom_booking_backend.equipment.mapper;


import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.model.Equipment;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class EquipmentMapper {
    public EquipmentResponseDto toEquipmentResponseDto(Equipment equipment) {
        return EquipmentResponseDto.builder()
                .id(equipment.getId())
                .title(equipment.getTitle())
                .description(equipment.getDescription())
                .modelNumber(equipment.getModelNumber())
                .videoUrl(equipment.getVideoUrl())
                .picture(equipment.getPicture())
                .brand(equipment.getBrand())
                .tag(equipment.getTag())
                .boardroomId(equipment.getBoardroom().getId())
                .isDisposed(equipment.isDisposed())
                .build();
    }

    public List<EquipmentResponseDto> toEquipmentResponseDtoList(List<Equipment> equipments) {
        return equipments.stream().map(this::toEquipmentResponseDto).toList();
    }

   public Equipment toEquipment(EquipmentDto equipmentDto) {
        return Equipment.builder()
                .title(equipmentDto.title())
                .description(equipmentDto.description())
                .modelNumber(equipmentDto.modelNumber())
                .videoUrl(equipmentDto.videoUrl())
                .picture(equipmentDto.picture())
                .brand(equipmentDto.brand())
                .disposed(equipmentDto.isDisposed())
                .build();
    }
}
