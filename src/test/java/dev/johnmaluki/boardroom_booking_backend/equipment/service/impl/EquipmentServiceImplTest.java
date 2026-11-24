package dev.johnmaluki.boardroom_booking_backend.equipment.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomServiceUtil;
import dev.johnmaluki.boardroom_booking_backend.core.cache.CacheKeys;
import dev.johnmaluki.boardroom_booking_backend.core.cache.ReactiveCacheService;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.mapper.EquipmentMapper;
import dev.johnmaluki.boardroom_booking_backend.equipment.model.Equipment;
import dev.johnmaluki.boardroom_booking_backend.equipment.repository.EquipmentRepository;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The cache is stubbed to pass straight through to its loader, so these tests exercise the
 * service's own logic rather than Redis behaviour.
 */
class EquipmentServiceImplTest {

  @Mock private EquipmentRepository equipmentRepository;
  @Mock private BoardroomServiceUtil boardroomServiceUtil;
  @Mock private ReactiveCacheService cacheService;

  private EquipmentServiceImpl equipmentService;
  private AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
    equipmentService =
        new EquipmentServiceImpl(
            equipmentRepository, new EquipmentMapper(), boardroomServiceUtil, cacheService);
  }

  @Test
  void getAllEquipments_returnsEmptyWhenNoEquipments() {
    when(equipmentRepository.findByArchivedFalseAndDeletedFalse()).thenReturn(Flux.empty());
    passCacheThrough();

    StepVerifier.create(equipmentService.getAllEquipments()).verifyComplete();

    verify(equipmentRepository).findByArchivedFalseAndDeletedFalse();
  }

  @Test
  void getAllEquipments_mapsEachRowToItsResponse() {
    Equipment equipment = new Equipment();
    equipment.setId(7L);
    equipment.setTitle("Projector");
    equipment.setBoardroomId(3L);
    equipment.setTag("tag-7");
    when(equipmentRepository.findByArchivedFalseAndDeletedFalse())
        .thenReturn(Flux.just(equipment));
    passCacheThrough();

    StepVerifier.create(equipmentService.getAllEquipments())
        .expectNextMatches(
            dto -> dto.id() == 7L && "Projector".equals(dto.title()) && dto.boardroomId() == 3L)
        .verifyComplete();
  }

  @Test
  void createEquipment_attachesTheBoardroomAndEvictsCaches() {
    Boardroom boardroom = new Boardroom();
    boardroom.setId(3L);
    when(boardroomServiceUtil.findBoardroomById(3L)).thenReturn(Mono.just(boardroom));
    when(equipmentRepository.save(any(Equipment.class)))
        .thenAnswer(
            invocation -> {
              Equipment saved = invocation.getArgument(0);
              saved.setId(11L);
              return Mono.just(saved);
            });
    when(cacheService.evictByPattern(anyString())).thenReturn(Mono.empty());

    EquipmentDto request =
        EquipmentDto.builder()
            .title("Whiteboard")
            .description("Wall mounted")
            .picture("1.jpg")
            .modelNumber("WB-1")
            .brand("Acme")
            .boardroomId(3L)
            .build();

    StepVerifier.create(equipmentService.createEquipment(request))
        .expectNextMatches(dto -> dto.id() == 11L && dto.boardroomId() == 3L)
        .verifyComplete();

    verify(cacheService).evictByPattern(CacheKeys.EQUIPMENT_NAMESPACE);
    verify(cacheService).evictByPattern(CacheKeys.BOARDROOM_NAMESPACE);
  }

  @Test
  void removeEquipment_softDeletesRatherThanDeleting() {
    Equipment equipment = new Equipment();
    equipment.setId(5L);
    when(equipmentRepository.findByIdAndArchivedFalseAndDeletedFalse(5L))
        .thenReturn(Mono.just(equipment));
    when(equipmentRepository.save(any(Equipment.class))).thenReturn(Mono.just(equipment));
    when(cacheService.evictByPattern(anyString())).thenReturn(Mono.empty());

    StepVerifier.create(equipmentService.removeEquipment(5L)).verifyComplete();

    org.junit.jupiter.api.Assertions.assertTrue(equipment.getDeleted());
  }

  @Test
  void removeEquipment_reportsNotFound() {
    when(equipmentRepository.findByIdAndArchivedFalseAndDeletedFalse(404L))
        .thenReturn(Mono.empty());
    // The eviction Mono is assembled up front even on the error path, so it still needs a stub.
    when(cacheService.evictByPattern(anyString())).thenReturn(Mono.empty());

    StepVerifier.create(equipmentService.removeEquipment(404L))
        .verifyErrorMessage("Equipment not found");
  }

  @SuppressWarnings("unchecked")
  private void passCacheThrough() {
    when(cacheService.cacheList(
            anyString(), any(Duration.class), eq(EquipmentResponseDto.class), any(Flux.class)))
        .thenAnswer(invocation -> invocation.getArgument(3));
  }

  @org.junit.jupiter.api.AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }
}
