package dev.johnmaluki.boardroom_booking_backend.equipment.service.impl;

import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.mapper.EquipmentMapper;
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
    @Override
    public List<EquipmentResponseDto> getAllEquipments() {
        return equipmentMapper.toEquipmentResponseDtoList(
                equipmentRepository.findAll()
        );
    }

}
