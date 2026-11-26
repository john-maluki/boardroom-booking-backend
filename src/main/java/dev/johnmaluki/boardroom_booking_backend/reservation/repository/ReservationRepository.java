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
        AND r.approval_status <> 'DECLINED'
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
        AND r.approval_status <> 'DECLINED'
      """)
  Flux<dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom>
      findBoardroomsWithConflictingEvents(
          @Param("startLocalDateTime") LocalDateTime startLocalDateTime,
          @Param("endLocalDateTime") LocalDateTime endLocalDateTime);

  /**
   * The bookings (up to 5, earliest first) that clash with a window, for telling the user what a
   * requested time conflicts with. Declined bookings don't hold the room, so they never clash.
   */
  @Query(
      """
      SELECT r.* FROM reservations r
      WHERE (:reservationId IS NULL OR r.id <> :reservationId)
        AND r.boardroom_id = :boardroomId
        AND r.start_date_time < :endLocalDateTime
        AND r.end_date_time > :startLocalDateTime
        AND r.deleted = false AND r.archived = false
        AND r.approval_status <> 'DECLINED'
      ORDER BY r.start_date_time
      LIMIT 5
      """)
  Flux<Reservation> findBoardroomConflictingEvents(
      @Param("reservationId") Long reservationId,
      @Param("boardroomId") Long boardroomId,
      @Param("startLocalDateTime") LocalDateTime startLocalDateTime,
      @Param("endLocalDateTime") LocalDateTime endLocalDateTime);

  /**
   * Takes a transaction-scoped PostgreSQL advisory lock for one boardroom. Booking paths call this
   * inside their transaction before checking for clashes, so two requests for the same room are
   * handled one after the other instead of both passing the check and double-booking the room.
   * The lock is released automatically when the transaction ends. 1001 namespaces these locks.
   */
  @Query("SELECT 1 FROM (SELECT pg_advisory_xact_lock(1001, CAST(:boardroomId AS integer))) AS lock")
  Mono<Integer> lockBoardroomForBooking(@Param("boardroomId") Long boardroomId);
}
