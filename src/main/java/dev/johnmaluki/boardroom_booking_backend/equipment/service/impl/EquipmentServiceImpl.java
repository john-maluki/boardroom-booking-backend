package dev.johnmaluki.boardroom_booking_backend.equipment.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomServiceUtil;
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
                equipmentRepository.findAll()
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

}
