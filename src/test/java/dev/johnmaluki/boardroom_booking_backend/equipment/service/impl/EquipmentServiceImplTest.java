package dev.johnmaluki.boardroom_booking_backend.equipment.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.mapper.EquipmentMapper;
import dev.johnmaluki.boardroom_booking_backend.equipment.model.Equipment;
import dev.johnmaluki.boardroom_booking_backend.equipment.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Collections;
import java.util.List;

public class EquipmentServiceImplTest {

  @Mock private EquipmentRepository equipmentRepository;

  @Mock private EquipmentMapper equipmentMapper;

  @InjectMocks private EquipmentServiceImpl equipmentServiceImpl;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  //  @Test
  //  void getAllEquipments_returnsEquipmentResponseDtoList() {
  //    Equipment equipment = new Equipment();
  //    List<Equipment> equipmentList = Collections.singletonList(equipment);
  //    EquipmentResponseDto equipmentResponseDto = new EquipmentResponseDto();
  //    List<EquipmentResponseDto> equipmentResponseDtoList =
  //        Collections.singletonList(equipmentResponseDto);
  //
  //    when(equipmentRepository.findAll()).thenReturn(equipmentList);
  //    when(equipmentMapper.toEquipmentResponseDtoList(anyList()))
  //        .thenReturn(equipmentResponseDtoList);
  //
  //    List<EquipmentResponseDto> result = equipmentServiceImpl.getAllEquipments();
  //
  //    assertEquals(equipmentResponseDtoList, result);
  //    verify(equipmentRepository).findAll();
  //    verify(equipmentMapper).toEquipmentResponseDtoList(anyList());
  //  }

  @Test
  void getAllEquipments_returnsEmptyListWhenNoEquipments() {
    when(equipmentRepository.findAll()).thenReturn(Collections.emptyList());
    when(equipmentMapper.toEquipmentResponseDtoList(anyList())).thenReturn(Collections.emptyList());

    List<EquipmentResponseDto> result = equipmentServiceImpl.getAllEquipments();

    assertTrue(result.isEmpty());
    verify(equipmentRepository).findAll();
    verify(equipmentMapper).toEquipmentResponseDtoList(anyList());
  }
}
