package dev.johnmaluki.boardroom_booking_backend.equipment.service;

import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EquipmentService {
  Flux<EquipmentResponseDto> getAllEquipments();

  Mono<EquipmentResponseDto> createEquipment(EquipmentDto equipmentDto);

  Mono<Void> removeEquipment(long equipmentId);

  Mono<EquipmentResponseDto> updateEquipment(long equipmentId, EquipmentDto equipmentDto);
}
