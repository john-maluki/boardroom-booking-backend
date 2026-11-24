package dev.johnmaluki.boardroom_booking_backend.reservation.repository;

import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import java.time.LocalDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReservationRepository extends ReactiveCrudRepository<Reservation, Long> {

  Mono<Reservation> findByIdAndArchivedFalseAndDeletedFalse(long id);

  /**
   * Overlap check that ignores one reservation, used when rescheduling or moving an existing
   * booking so it does not conflict with itself. Only the count matters to the caller.
   */
  @Query(
      """
      SELECT count(*) FROM reservations r
      WHERE (:reservationId IS NULL OR r.id <> :reservationId)
        AND r.boardroom_id = :boardroomId
        AND r.start_date_time < :endLocalDateTime
        AND r.end_date_time > :startLocalDateTime
        AND r.deleted = false AND r.archived = false
      """)
  Mono<Long> countBoardroomConflictingEvents(
      @Param("reservationId") Long reservationId,
      @Param("boardroomId") Long boardroomId,
      @Param("startLocalDateTime") LocalDateTime startLocalDateTime,
      @Param("endLocalDateTime") LocalDateTime endLocalDateTime);

  /** Boardrooms that already have a booking clashing with the requested window. */
  @Query(
      """
      SELECT DISTINCT b.* FROM boardrooms b
      JOIN reservations r ON r.boardroom_id = b.id
      WHERE r.start_date_time < :endLocalDateTime
        AND r.end_date_time > :startLocalDateTime
        AND r.deleted = false AND r.archived = false
      """)
  Flux<dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom>
      findBoardroomsWithConflictingEvents(
          @Param("startLocalDateTime") LocalDateTime startLocalDateTime,
          @Param("endLocalDateTime") LocalDateTime endLocalDateTime);
}
