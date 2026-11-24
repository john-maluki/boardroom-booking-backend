package dev.johnmaluki.boardroom_booking_backend.equipment.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomServiceUtil;
import dev.johnmaluki.boardroom_booking_backend.core.cache.CacheKeys;
import dev.johnmaluki.boardroom_booking_backend.core.cache.CacheTtl;
import dev.johnmaluki.boardroom_booking_backend.core.cache.ReactiveCacheService;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.mapper.EquipmentMapper;
import dev.johnmaluki.boardroom_booking_backend.equipment.model.Equipment;
import dev.johnmaluki.boardroom_booking_backend.equipment.repository.EquipmentRepository;
import dev.johnmaluki.boardroom_booking_backend.equipment.service.EquipmentService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Equipment is read far more often than it changes — it is shown on every boardroom page — so the
 * listing is served from Redis and every write drops both the equipment namespace and the boardroom
 * namespace, since a boardroom's equipment list is cached under the boardroom too.
 */
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {
  private final EquipmentRepository equipmentRepository;
  private final EquipmentMapper equipmentMapper;
  private final BoardroomServiceUtil boardroomServiceUtil;
  private final ReactiveCacheService cacheService;

  @Override
  public Flux<EquipmentResponseDto> getAllEquipments() {
    return cacheService.cacheList(
        CacheKeys.EQUIPMENT_ALL,
        CacheTtl.REFERENCE_DATA,
        EquipmentResponseDto.class,
        equipmentRepository
            .findByArchivedFalseAndDeletedFalse()
            .map(equipmentMapper::toEquipmentResponseDto));
  }

  @Override
  @Transactional
  public Mono<EquipmentResponseDto> createEquipment(EquipmentDto equipmentDto) {
    return boardroomServiceUtil
        .findBoardroomById(equipmentDto.boardroomId())
        .flatMap(
            boardroom -> {
              Equipment equipment = equipmentMapper.toEquipment(equipmentDto);
              equipment.setBoardroomId(boardroom.getId());
              return equipmentRepository.save(equipment);
            })
        .map(equipmentMapper::toEquipmentResponseDto)
        .flatMap(dto -> evictCaches().thenReturn(dto));
  }

  @Override
  @Transactional
  public Mono<Void> removeEquipment(long equipmentId) {
    return getEquipmentById(equipmentId)
        .flatMap(
            equipment -> {
              equipment.setDeleted(true);
              return equipmentRepository.save(equipment);
            })
        .then(evictCaches());
  }

  @Override
  @Transactional
  public Mono<EquipmentResponseDto> updateEquipment(long equipmentId, EquipmentDto equipmentDto) {
    return getEquipmentById(equipmentId)
        .flatMap(
            equipment -> {
              equipment.setBrand(equipmentDto.brand());
              equipment.setDisposed(equipmentDto.isDisposed());
              equipment.setDescription(equipmentDto.description());
              equipment.setPicture(equipmentDto.picture());
              equipment.setModelNumber(equipmentDto.modelNumber());
              equipment.setTitle(equipmentDto.title());
              equipment.setVideoUrl(equipmentDto.videoUrl());

              if (!Objects.equals(equipment.getBoardroomId(), equipmentDto.boardroomId())) {
                // Confirm the new boardroom exists before re-homing the equipment.
                return boardroomServiceUtil
                    .findBoardroomById(equipmentDto.boardroomId())
                    .flatMap(
                        boardroom -> {
                          equipment.setBoardroomId(boardroom.getId());
                          return equipmentRepository.save(equipment);
                        });
              }
              return equipmentRepository.save(equipment);
            })
        .map(equipmentMapper::toEquipmentResponseDto)
        .flatMap(dto -> evictCaches().thenReturn(dto));
  }

  private Mono<Equipment> getEquipmentById(long equipmentId) {
    return equipmentRepository
        .findByIdAndArchivedFalseAndDeletedFalse(equipmentId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Equipment not found")));
  }

  private Mono<Void> evictCaches() {
    return cacheService
        .evictByPattern(CacheKeys.EQUIPMENT_NAMESPACE)
        .then(cacheService.evictByPattern(CacheKeys.BOARDROOM_NAMESPACE));
  }
}
