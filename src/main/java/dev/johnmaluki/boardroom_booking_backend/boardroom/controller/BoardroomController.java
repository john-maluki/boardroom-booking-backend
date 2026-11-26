package dev.johnmaluki.boardroom_booking_backend.boardroom.controller;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomAdminDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomEventFilterResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockMessageDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockedBoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomService;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationOverlapResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Boardroom")
public class BoardroomController {
  private final BoardroomService boardroomService;

  @GetMapping("/boardrooms")
  public Flux<BoardroomResponseDto> getAllBoardrooms() {
    return boardroomService.getAllBoardrooms();
  }

  @GetMapping("/locked-boardrooms")
  @Operation(summary = "fetch all locked boardrooms")
  public Flux<BoardroomResponseDto> getLockedBoardrooms() {
    return boardroomService.getLockedBoardrooms();
  }

  @GetMapping("/boardrooms/{boardroomId}/reservations")
  public Flux<ReservationResponseDto> getBoardroomsReservations(
      @PathVariable("boardroomId") long boardroomId) {
    return boardroomService.getBoardroomReservations(boardroomId);
  }

  @PostMapping("/boardrooms/{boardroomId}/event-overlap")
  @Operation(summary = "Check reservation event overlap")
  public Mono<ReservationOverlapResponseDto> checkReservationEventOverlap(
      @PathVariable("boardroomId") long boardroomId,
      @RequestParam(value = "excludeReservationId", required = false) Long excludeReservationId,
      @RequestBody @Valid ReservationEventDateDto reservationEventDateDto) {
    return boardroomService.checkBoardroomReservationOverlap(
        boardroomId, reservationEventDateDto, excludeReservationId);
  }

  @GetMapping("/boardrooms/{boardroomId}/archived-reservations")
  public Flux<ReservationResponseDto> getBoardroomsArchivedReservations(
      @PathVariable("boardroomId") long boardroomId) {
    return boardroomService.getBoardroomArchivedReservations(boardroomId);
  }

  @GetMapping("/boardrooms/{boardroomId}")
  public Mono<BoardroomResponseDto> getBoardroomById(
      @PathVariable("boardroomId") long boardroomId) {
    return boardroomService.getBoardroomById(boardroomId);
  }

  @PatchMapping("/boardrooms/{boardroomId}")
  @Operation(summary = "Update boardroom")
  public Mono<BoardroomResponseDto> updateBoardroomById(
      @PathVariable("boardroomId") long boardroomId, @RequestBody @Valid BoardroomDto boardroomDto) {
    return boardroomService.updateBoardroomById(boardroomId, boardroomDto);
  }

  @GetMapping("/boardrooms/{boardroomId}/equipments")
  public Flux<EquipmentResponseDto> getBoardroomEquipments(
      @PathVariable("boardroomId") long boardroomId) {
    return boardroomService.getBoardroomEquipments(boardroomId);
  }

  @GetMapping("/boardrooms/{boardroomId}/administrator")
  public Mono<UserResponseDto> getBoardroomAdministrator(
      @PathVariable("boardroomId") long boardroomId) {
    return boardroomService.getBoardroomAdministrator(boardroomId);
  }

  @GetMapping("/boardrooms/{boardroomId}/contacts")
  public Flux<BoardroomContactResponseDto> getBoardroomContacts(
      @PathVariable("boardroomId") long boardroomId) {
    return boardroomService.getBoardroomContacts(boardroomId);
  }

  @GetMapping("/boardrooms/{boardroomId}/lock-message")
  public Mono<LockedBoardroomResponseDto> getLockedBoardroomReasonById(
      @PathVariable("boardroomId") long boardroomId) {
    return boardroomService.getLockedBoardroomReasonById(boardroomId);
  }

  @PostMapping("/boardrooms")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new boardroom")
  public Mono<BoardroomResponseDto> createBoardroom(@RequestBody @Valid BoardroomDto boardroomDto) {
    return boardroomService.createBoardroom(boardroomDto);
  }

  @PatchMapping("/boardrooms/{boardroomId}/administrator")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create or Change boardroom administrator")
  public Mono<UserResponseDto> createBoardroomAdministrator(
      @PathVariable("boardroomId") long boardroomId,
      @RequestBody @Valid BoardroomAdminDto boardroomAdminDto) {
    return boardroomService.createBoardroomAdministrator(boardroomId, boardroomAdminDto);
  }

  @PostMapping("/boardrooms/{boardroomId}/contacts")
  @Operation(summary = "Create a boardroom contact")
  public Mono<BoardroomContactResponseDto> createBoardroomContact(
      @PathVariable("boardroomId") long boardroomId,
      @RequestBody @Valid BoardroomContactDto boardroomContactDto) {
    return boardroomService.createBoardroomContact(boardroomId, boardroomContactDto);
  }

  @DeleteMapping("/boardrooms/{boardroomId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete boardroom")
  public Mono<Void> deleteBoardroomById(@PathVariable("boardroomId") long boardroomId) {
    return boardroomService.removeBoardroomById(boardroomId);
  }

  @DeleteMapping("/boardrooms/{boardroomId}/contacts/{contactId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete boardroom contact")
  public Mono<Void> removeBoardroomContact(
      @PathVariable("boardroomId") long boardroomId, @PathVariable("contactId") long contactId) {
    return boardroomService.removeBoardroomContact(boardroomId, contactId);
  }

  @PatchMapping("/boardrooms/{boardroomId}/contacts/{contactId}")
  @Operation(summary = "Update boardroom contact")
  public Mono<BoardroomContactResponseDto> updateBoardroomContact(
      @PathVariable("boardroomId") long boardroomId,
      @PathVariable("contactId") long contactId,
      @RequestBody @Valid BoardroomContactDto boardroomContactDto) {
    return boardroomService.updateBoardroomContact(boardroomId, contactId, boardroomContactDto);
  }

  @PostMapping("/boardrooms/{boardroomId}/lock-message")
  @Operation(summary = "Lock boardroom. Provide lock message")
  public Mono<LockedBoardroomResponseDto> lockBoardroomById(
      @PathVariable("boardroomId") long boardroomId,
      @RequestBody @Valid LockMessageDto lockMessageDto) {
    return boardroomService.lockBoardroomById(boardroomId, lockMessageDto);
  }

  @PatchMapping("/boardrooms/{boardroomId}/unlock")
  @Operation(summary = "Unlock boardroom")
  public Mono<Map<String, String>> unLockBoardroomReason(
      @PathVariable("boardroomId") long boardroomId) {
    return boardroomService
        .unLockBoardroomById(boardroomId)
        .thenReturn(Map.of("message", "unlocked successfully"));
  }

  @PostMapping("/boardrooms/filter_by_event_date")
  @Operation(summary = "Filter boardrooms by event date ")
  public Flux<BoardroomEventFilterResponseDto> checkAvailableBoardroomByEventDate(
      @RequestBody @Valid ReservationEventDateDto reservationEventDateDto) {
    return boardroomService.filterAvailableBoardroomsByEventDate(reservationEventDateDto);
  }
}
