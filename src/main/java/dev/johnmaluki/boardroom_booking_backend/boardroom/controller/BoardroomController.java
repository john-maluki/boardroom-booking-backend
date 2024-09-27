package dev.johnmaluki.boardroom_booking_backend.boardroom.controller;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.*;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomService;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationOverlapResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationService;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Boardroom")
public class BoardroomController {
    private final BoardroomService boardroomService;
    private final ReservationService reservationService;

    @GetMapping("/boardrooms")
    public ResponseEntity<List<BoardroomResponseDto>> getAllBoardrooms() {
        return ResponseEntity.ok(boardroomService.getAllBoardrooms());
    }

    @GetMapping("/locked-boardrooms")
    @Operation(summary = "fetch all locked boardrooms")
    public ResponseEntity<List<BoardroomResponseDto>> getLockedBoardrooms() {
        return ResponseEntity.ok(boardroomService.getLockedBoardrooms());
    }

    @GetMapping("/boardrooms/{boardroomId}/reservations")
    public ResponseEntity<List<ReservationResponseDto>> getBoardroomsReservations(
            @PathVariable("boardroomId") long boardroomId
    ) {
        return ResponseEntity.ok(boardroomService.getBoardroomReservations(boardroomId));
    }

    @PostMapping("/boardrooms/{boardroomId}/event-overlap")
    @Operation(summary = "Check reservation event overlap")
    public ResponseEntity<ReservationOverlapResponseDto> checkReservationEventOverlap(
            @PathVariable("boardroomId") long boardroomId,
            @RequestBody @Valid ReservationEventDateDto reservationEventDateDto
    ){
        return ResponseEntity.ok(boardroomService.checkBoardroomReservationOverlap(boardroomId, reservationEventDateDto));
    }

    @GetMapping("/boardrooms/{boardroomId}/archived-reservations")
    public ResponseEntity<List<ReservationResponseDto>> getBoardroomsArchivedReservations(
            @PathVariable("boardroomId") long boardroomId
    ) {
        return ResponseEntity.ok(boardroomService.getBoardroomArchivedReservations(boardroomId));
    }

    @GetMapping("/boardrooms/{boardroomId}")
    public ResponseEntity<BoardroomResponseDto> getBoardroomById(@PathVariable("boardroomId") long boardroomId) {
        return ResponseEntity.ok(boardroomService.getBoardroomById(boardroomId));
    }

    @PatchMapping("/boardrooms/{boardroomId}")
    @Operation(summary = "Update boardroom")
    public ResponseEntity<BoardroomResponseDto> updateBoardroomById(
            @PathVariable("boardroomId") long boardroomId,
            @RequestBody @Valid BoardroomDto boardroomDto
    ) {
        return ResponseEntity.ok(boardroomService.updateBoardroomById(boardroomId, boardroomDto));
    }

    @GetMapping("/boardrooms/{boardroomId}/equipments")
    public ResponseEntity<List<EquipmentResponseDto>> getBoardroomEquipments(@PathVariable("boardroomId") long boardroomId) {
        return ResponseEntity.ok(boardroomService.getBoardroomEquipments(boardroomId));
    }

    @GetMapping("/boardrooms/{boardroomId}/administrator")
    public ResponseEntity<UserResponseDto> getBoardroomAdministrator(@PathVariable("boardroomId") long boardroomId) {
        return ResponseEntity.ok(boardroomService.getBoardroomAdministrator(boardroomId));
    }

    @GetMapping("/boardrooms/{boardroomId}/contacts")
    public ResponseEntity<List<BoardroomContactResponseDto>> getBoardroomContacts(@PathVariable("boardroomId") long boardroomId) {
        return ResponseEntity.ok(boardroomService.getBoardroomContacts(boardroomId));
    }

    @GetMapping("/boardrooms/{boardroomId}/lock-message")
    public ResponseEntity<LockedBoardroomResponseDto> getLockedBoardroomReasonById(
            @PathVariable("boardroomId") long boardroomId
    ) {
        return ResponseEntity.ok(boardroomService.getLockedBoardroomReasonById(boardroomId));
    }

    @PostMapping("/boardrooms")
    @Operation(summary = "Create a new boardroom")
    public ResponseEntity<BoardroomResponseDto> createBoardroom(
            @RequestBody @Valid BoardroomDto boardroomDto
    ) {
        return new ResponseEntity<>(boardroomService.createBoardroom(boardroomDto), HttpStatus.CREATED);
    }

    @PatchMapping("/boardrooms/{boardroomId}/administrator")
    @Operation(summary = "Create or Change boardroom administrator")
    public ResponseEntity<UserResponseDto> createBoardroomAdministrator(
            @PathVariable("boardroomId") long boardroomId,
            @RequestBody @Valid BoardroomAdminDto boardroomAdminDto
            ) {
        return new ResponseEntity<>(
                boardroomService.createBoardroomAdministrator(boardroomId, boardroomAdminDto),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/boardrooms/{boardroomId}/contacts")
    @Operation(summary = "Create a boardroom contact")
    public ResponseEntity<BoardroomContactResponseDto> createBoardroomContact(
            @PathVariable("boardroomId") long boardroomId,
            @RequestBody @Valid BoardroomContactDto boardroomContactDto
    ) {
        return ResponseEntity.ok(boardroomService.createBoardroomContact(boardroomId, boardroomContactDto));
    }

    @DeleteMapping("/boardrooms/{boardroomId}")
    @Operation(summary = "Delete boardroom")
    public ResponseEntity<Void> deleteBoardroomById(@PathVariable("boardroomId") long boardroomId) {
        boardroomService.removeBoardroomById(boardroomId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/boardrooms/{boardroomId}/contacts/{contactId}")
    @Operation(summary = "Delete boardroom contact")
    public ResponseEntity<Void> removeBoardroomContact(
            @PathVariable("boardroomId") long boardroomId,
            @PathVariable("contactId") long contactId
    ) {
        boardroomService.removeBoardroomContact(boardroomId, contactId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/boardrooms/{boardroomId}/contacts/{contactId}")
    @Operation(summary = "Update boardroom contact")
    public ResponseEntity<BoardroomContactResponseDto> updateBoardroomContact(
            @PathVariable("boardroomId") long boardroomId,
            @PathVariable("contactId") long contactId,
            @RequestBody @Valid BoardroomContactDto boardroomContactDto
    ) {
        return ResponseEntity.ok(boardroomService.updateBoardroomContact(boardroomId, contactId, boardroomContactDto));
    }

    @PostMapping("/boardrooms/{boardroomId}/lock-message")
    @Operation(summary = "Lock boardroom. Provide lock message")
    public ResponseEntity<LockedBoardroomResponseDto> lockBoardroomById(
            @PathVariable("boardroomId") long boardroomId,
            @RequestBody @Valid LockMessageDto lockMessageDto
    ) {
        return ResponseEntity.ok(boardroomService.lockBoardroomById(boardroomId, lockMessageDto));
    }

    @PatchMapping("/boardrooms/{boardroomId}/unlock")
    @Operation(summary = "Unlock boardroom")
    public ResponseEntity<Map<String, String>> unLockBoardroomReason(
            @PathVariable("boardroomId") long boardroomId
    ) {
        boardroomService.unLockBoardroomById(boardroomId);
        return ResponseEntity.ok(Map.of("message", "unlocked successfully"));
    }

}
