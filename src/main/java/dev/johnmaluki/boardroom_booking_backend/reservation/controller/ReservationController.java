package dev.johnmaluki.boardroom_booking_backend.reservation.controller;

import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ApproveReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ChangeVenueDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationMeetingLinkDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Returning {@code Flux} and {@code Mono} directly rather than {@code ResponseEntity} keeps the
 * response streaming: WebFlux writes each element as it arrives instead of buffering the whole
 * list. Status codes that are not 200 are declared with {@code @ResponseStatus}.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reservation")
public class ReservationController {
  private final ReservationService reservationService;

  @GetMapping("/reservations")
  public Flux<ReservationResponseDto> getAllReservations() {
    return reservationService.getAllReservations();
  }

  @GetMapping("/reservations/{reservationId}")
  @Operation(summary = "Fetch reservation by its id")
  public Mono<ReservationResponseDto> getReservationById(
      @PathVariable("reservationId") long reservationId) {
    return reservationService.getReservationById(reservationId);
  }

  @GetMapping("/upcoming-reservations")
  public Flux<ReservationResponseDto> getUpcomingReservations() {
    return reservationService.getUpcomingReservations();
  }

  @GetMapping("/live-meetings")
  public Flux<ReservationResponseDto> getLiveMeetings() {
    return reservationService.getAllLiveMeetings();
  }

  @GetMapping("/archived-reservations")
  public Flux<ReservationResponseDto> getArchivedReservations() {
    return reservationService.getArchivedReservations();
  }

  @PostMapping("/reservations")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create reservation")
  public Mono<ReservationResponseDto> createReservation(
      @RequestBody @Valid ReservationDto reservationDto) {
    return reservationService.createReservation(reservationDto);
  }

  @PatchMapping("/reservations/{reservationId}")
  @Operation(summary = "update reservation")
  public Mono<ReservationResponseDto> updateReservation(
      @PathVariable("reservationId") long reservationId,
      @RequestBody @Valid ReservationDto reservationDto) {
    return reservationService.updateReservation(reservationId, reservationDto);
  }

  @PatchMapping("/reservations/{reservationId}/approve")
  @Operation(summary = "Approve reservation")
  public Mono<ReservationResponseDto> approveReservation(
      @PathVariable("reservationId") long reservationId,
      @RequestBody @Valid ApproveReservationDto approveReservationDto) {
    return reservationService.approveReservation(reservationId, approveReservationDto);
  }

  @PatchMapping("/reservations/{reservationId}/add-meeting-link")
  @Operation(summary = "Update reservation with a meet link")
  public Mono<ReservationResponseDto> updateReservationWithMeetLinK(
      @PathVariable("reservationId") long reservationId,
      @RequestBody @Valid ReservationMeetingLinkDto reservationMeetingLinkDto) {
    return reservationService.updateReservationWithMeetingLink(
        reservationId, reservationMeetingLinkDto);
  }

  @PatchMapping("/reservations/{reservationId}/change-venue")
  @Operation(summary = "Change reservation venue")
  public Mono<ReservationResponseDto> changeReservationVenue(
      @PathVariable("reservationId") long reservationId,
      @RequestBody @Valid ChangeVenueDto changeVenueDto) {
    return reservationService.changeReservationVenue(reservationId, changeVenueDto);
  }

  @PatchMapping("/reservations/{reservationId}/reschedule")
  @Operation(summary = "Reschedule reservation")
  public Mono<ReservationResponseDto> rescheduleReservation(
      @PathVariable("reservationId") long reservationId,
      @RequestBody @Valid ReservationEventDateDto reservationEventDateDto) {
    return reservationService.rescheduleReservation(reservationId, reservationEventDateDto);
  }

  @DeleteMapping("/reservations/{reservationId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Remove reservation")
  public Mono<Void> removeReservation(@PathVariable("reservationId") long reservationId) {
    return reservationService.removeReservation(reservationId);
  }
}
