package dev.johnmaluki.boardroom_booking_backend.boardroom.service;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
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
}
