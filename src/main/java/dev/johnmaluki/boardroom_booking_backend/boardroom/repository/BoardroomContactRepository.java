package dev.johnmaluki.boardroom_booking_backend.boardroom.repository;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.BoardroomContact;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BoardroomContactRepository
    extends ReactiveCrudRepository<BoardroomContact, Long> {

  Flux<BoardroomContact> findByBoardroomIdAndArchivedFalseAndDeletedFalse(long boardroomId);

  Mono<BoardroomContact> findByIdAndBoardroomIdAndArchivedFalseAndDeletedFalse(
      long contactId, long boardroomId);
}
