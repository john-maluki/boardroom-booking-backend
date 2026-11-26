package dev.johnmaluki.boardroom_booking_backend.boardroom.service;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationConflictDto;
import java.time.LocalDateTime;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** The parts of the boardroom service other aggregates need. */
public interface BoardroomServiceUtil {
  /** Errors with {@code ResourceNotFoundException} when the boardroom does not exist. */
  Mono<Boardroom> findBoardroomById(long boardroomId);

  /**
   * Whether the requested window clashes with an existing booking.
   *
   * @param reservationId a reservation to exclude, so an existing booking does not clash with
   *     itself when being rescheduled or moved; {@code null} to consider every booking
   */
  Mono<Boolean> hasReservationOverlap(
      Long reservationId, Long boardroomId, ReservationEventDateDto reservationEventDateDto);

  /**
   * Errors with {@code ReservationOverlapException} when this reservation's window clashes with
   * another booking in the same boardroom.
   */
  Mono<Void> assertNoReservationOverlap(Reservation reservation);

  /**
   * The bookings that clash with a window in a boardroom (declined bookings never clash).
   *
   * @param reservationId a reservation to exclude, or {@code null}
   */
  Flux<ReservationConflictDto> findConflicts(
      Long reservationId, Long boardroomId, LocalDateTime start, LocalDateTime end);

  /**
   * Serialises bookings for one boardroom for the rest of the current transaction, so that a
   * clash check followed by a save can't race with another request for the same room. Must be
   * called inside a {@code @Transactional} method.
   */
  Mono<Void> lockBoardroomForBooking(long boardroomId);
}
