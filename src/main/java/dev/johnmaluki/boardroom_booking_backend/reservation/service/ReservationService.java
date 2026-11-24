package dev.johnmaluki.boardroom_booking_backend.reservation.service;

import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ApproveReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ChangeVenueDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationMeetingLinkDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReservationService {
  Flux<ReservationResponseDto> getAllReservations();

  /**
   * Meeting reservations scheduled to take place in the future.
   *
   * @return upcoming reservations
   */
  Flux<ReservationResponseDto> getUpcomingReservations();

  /**
   * Live meetings, where the current date and time fall between a meeting's start and end.
   *
   * @return meetings currently in progress
   */
  Flux<ReservationResponseDto> getAllLiveMeetings();

  Flux<ReservationResponseDto> getArchivedReservations();

  Mono<ReservationResponseDto> getReservationById(long reservationId);

  Mono<ReservationResponseDto> createReservation(ReservationDto reservationDto);

  Mono<ReservationResponseDto> approveReservation(
      long reservationId, ApproveReservationDto approveReservationDto);

  Mono<ReservationResponseDto> updateReservationWithMeetingLink(
      long reservationId, ReservationMeetingLinkDto reservationMeetingLinkDto);

  Mono<ReservationResponseDto> changeReservationVenue(
      long reservationId, ChangeVenueDto changeVenueDto);

  Mono<ReservationResponseDto> rescheduleReservation(
      long reservationId, ReservationEventDateDto reservationEventDateDto);

  Mono<Void> removeReservation(long reservationId);

  Mono<ReservationResponseDto> updateReservation(
      long reservationId, ReservationDto reservationDto);
}
