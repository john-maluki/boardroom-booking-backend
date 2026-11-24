package dev.johnmaluki.boardroom_booking_backend.boardroom.repository;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface LockedRoomRepository extends ReactiveCrudRepository<LockedRoom, Long> {
  Mono<LockedRoom> findByBoardroomIdAndLockedTrueAndArchivedFalseAndDeletedFalse(long boardroomId);
}
