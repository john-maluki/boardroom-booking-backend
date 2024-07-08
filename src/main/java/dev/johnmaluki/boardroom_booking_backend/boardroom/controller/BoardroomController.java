package dev.johnmaluki.boardroom_booking_backend.boardroom.controller;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockedBoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomService;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
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
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BoardroomController {
    private final BoardroomService boardroomService;
    private final ReservationService reservationService;

    @GetMapping("/boardrooms")
    public ResponseEntity<List<BoardroomResponseDto>> getAllBoardrooms() {
        return ResponseEntity.ok(boardroomService.getAllBoardrooms());
    }

    @GetMapping("/boardrooms/{boardroomId}/reservations")
    public ResponseEntity<List<ReservationResponseDto>> getBoardroomsReservations(
            @PathVariable("boardroomId") long boardroomId
    ) {
        return ResponseEntity.ok(boardroomService.getBoardroomReservations(boardroomId));
    }

    @GetMapping("/boardrooms/{boardroomId}")
    public ResponseEntity<BoardroomResponseDto> getBoardroomById(@PathVariable("boardroomId") long boardroomId) {
        return ResponseEntity.ok(boardroomService.getBoardroomById(boardroomId));
    }

    @GetMapping("/boardrooms/{boardroomId}/equipments")
    public ResponseEntity<List<EquipmentResponseDto>> getBoardroomEquipments(@PathVariable("boardroomId") long boardroomId) {
        return ResponseEntity.ok(boardroomService.getBoardroomEquipments(boardroomId));
    }

    @GetMapping("/boardrooms/{boardroomId}/locked")
    public ResponseEntity<LockedBoardroomResponseDto> getLockedBoardroomReasonById(
            @PathVariable("boardroomId") long boardroomId
    ) {
        return ResponseEntity.ok(boardroomService.getLockedBoardroomReasonById(boardroomId));
    }
}
