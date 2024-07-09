package dev.johnmaluki.boardroom_booking_backend.equipment.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomServiceUtil;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.util.DataFilterUtil;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.mapper.EquipmentMapper;
import dev.johnmaluki.boardroom_booking_backend.equipment.model.Equipment;
import dev.johnmaluki.boardroom_booking_backend.equipment.repository.EquipmentRepository;
import dev.johnmaluki.boardroom_booking_backend.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;
    private final BoardroomServiceUtil boardroomServiceUtil;
    @Override
    public List<EquipmentResponseDto> getAllEquipments() {
        return equipmentMapper.toEquipmentResponseDtoList(
                new DataFilterUtil<Equipment>().removeArchivedAndDeletedRecords(
                        equipmentRepository.findAll()
                )
        );
    }

    @Override
    public EquipmentResponseDto createEquipment(EquipmentDto equipmentDto) {
        Boardroom boardroom = boardroomServiceUtil.findBoardroomById(equipmentDto.boardroomId());
        Equipment newEquipment = equipmentMapper.toEquipment(equipmentDto);
        boardroom.addEquipment(newEquipment);
        newEquipment = equipmentRepository.save(newEquipment);
        return equipmentMapper.toEquipmentResponseDto(newEquipment);
    }

    @Override
    public void removeEquipment(long equipmentId) {
        this.equipmentSoftDeletion(equipmentId);
    }

    private Equipment getEquipmentById(long equipmentId) {
        return equipmentRepository.findByIdAndArchivedFalseAndDeletedFalse(equipmentId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Equipment not found"));
    }

    private void equipmentSoftDeletion(long equipmentId) {
        Equipment equipment = this.getEquipmentById(equipmentId);
        equipment.setDeleted(true);
        equipmentRepository.save(equipment);
    }

}
