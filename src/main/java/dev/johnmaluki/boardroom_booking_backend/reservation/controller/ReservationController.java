package dev.johnmaluki.boardroom_booking_backend.reservation.controller;

import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ApproveReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
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

    @GetMapping("/reservation/{reservationId}")
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

    @PatchMapping("/reservations/{reservationId}")
    @Operation(summary = "Approve reservation")
    public ResponseEntity<ReservationResponseDto> approveReservation(
            @PathVariable("reservationId") long reservationId,
            @RequestBody @Valid ApproveReservationDto approveReservationDto
    ){
        return ResponseEntity.ok(reservationService.approveReservation(reservationId, approveReservationDto));
    }
}
