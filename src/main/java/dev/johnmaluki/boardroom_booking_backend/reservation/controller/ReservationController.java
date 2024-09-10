package dev.johnmaluki.boardroom_booking_backend.reservation.controller;

import dev.johnmaluki.boardroom_booking_backend.reservation.dto.*;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reservation")
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponseDto>> getAllReservations(){
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/reservations/{reservationId}")
    @Operation(summary = "Fetch reservation by its id")
    public ResponseEntity<ReservationResponseDto> getReservationById(
            @PathVariable("reservationId") long reservationId
    ) {
        return ResponseEntity.ok(reservationService.getReservationById(reservationId));
    }

    @GetMapping("/upcoming-reservations")
    public ResponseEntity<List<ReservationResponseDto>> getUpcomingReservations() {
        return ResponseEntity.ok(reservationService.getUpcomingReservations());
    }

    @GetMapping("/live-meetings")
    public ResponseEntity<List<ReservationResponseDto>> getLiveMeetings() {
        return ResponseEntity.ok(reservationService.getAllLiveMeetings());
    }

    @GetMapping("/archived-reservations")
    public ResponseEntity<List<ReservationResponseDto>> getArchivedReservations() {
        return ResponseEntity.ok(reservationService.getArchivedReservations());
    }

    @PostMapping("/reservations")
    @Operation(summary = "Create reservation")
    public ResponseEntity<ReservationResponseDto> createReservation(
            @RequestBody @Valid ReservationDto reservationDto
            ){
        return new ResponseEntity<>(
                reservationService.createReservation(reservationDto),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/reservations/{reservationId}/approve")
    @Operation(summary = "Approve reservation")
    public ResponseEntity<ReservationResponseDto> approveReservation(
            @PathVariable("reservationId") long reservationId,
            @RequestBody @Valid ApproveReservationDto approveReservationDto
    ){
        return ResponseEntity.ok(reservationService.approveReservation(reservationId, approveReservationDto));
    }

    @PatchMapping("/reservations/{reservationId}/add-meeting-link")
    @Operation(summary = "Update reservation with a meet link")
    public ResponseEntity<ReservationResponseDto> updateReservationWithMeetLinK(
            @PathVariable("reservationId") long reservationId,
            @RequestBody @Valid ReservationMeetingLinkDto reservationMeetingLinkDto
            ){
        return ResponseEntity.ok(reservationService.updateReservationWithMeetingLink(reservationId, reservationMeetingLinkDto));
    }

    @PatchMapping("/reservations/{reservationId}/change-venue")
    @Operation(summary = "Change reservation venue")
    public ResponseEntity<ReservationResponseDto> changeReservationVenue(
            @PathVariable("reservationId") long reservationId,
            @RequestBody @Valid ChangeVenueDto changeVenueDto
    ){
        return ResponseEntity.ok(reservationService.changeReservationVenue(reservationId, changeVenueDto));
    }

    @PatchMapping("/reservations/{reservationId}/reschedule")
    @Operation(summary = "Reschedule reservation")
    public ResponseEntity<ReservationResponseDto> rescheduleReservation(
            @PathVariable("reservationId") long reservationId,
            @RequestBody @Valid RescheduleReservationDto rescheduleReservationDto
    ){
        return ResponseEntity.ok(reservationService.rescheduleReservation(reservationId, rescheduleReservationDto));
    }

    @DeleteMapping("/reservations/{reservationId}")
    @Operation(summary = "Remove reservation")
    public ResponseEntity<Void> rescheduleReservation(
            @PathVariable("reservationId") long reservationId
    ){
        reservationService.removeReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}
