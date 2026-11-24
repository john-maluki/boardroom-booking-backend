package dev.johnmaluki.boardroom_booking_backend.boardroom.repository;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BoardroomRepository extends ReactiveCrudRepository<Boardroom, Long> {
  Flux<Boardroom> findByArchivedFalseAndDeletedFalse();

  Mono<Boardroom> findByIdAndArchivedFalseAndDeletedFalse(long boardroomId);

  Flux<Boardroom> findAllByLockedTrueAndDeletedFalseAndArchivedFalse();
}
