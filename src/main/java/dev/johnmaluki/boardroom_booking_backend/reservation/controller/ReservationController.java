package dev.johnmaluki.boardroom_booking_backend.reservation.controller;

import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponseDto>> getAllReservations(){
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ReservationResponseDto> getReservationById(
            @PathVariable("reservationId") long id
    ) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
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
}
