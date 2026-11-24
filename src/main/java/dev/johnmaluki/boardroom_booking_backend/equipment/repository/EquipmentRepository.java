package dev.johnmaluki.boardroom_booking_backend.equipment.repository;

import dev.johnmaluki.boardroom_booking_backend.equipment.model.Equipment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EquipmentRepository extends ReactiveCrudRepository<Equipment, Long> {
  Mono<Equipment> findByIdAndArchivedFalseAndDeletedFalse(long equipmentId);

  Flux<Equipment> findByArchivedFalseAndDeletedFalse();

  Flux<Equipment> findByBoardroomIdAndArchivedFalseAndDeletedFalse(long boardroomId);
}
