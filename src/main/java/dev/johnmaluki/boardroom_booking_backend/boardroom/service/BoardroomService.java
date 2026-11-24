package dev.johnmaluki.boardroom_booking_backend.boardroom.service;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomAdminDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomEventFilterResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockMessageDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockedBoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationOverlapResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BoardroomService {
  Flux<BoardroomResponseDto> getAllBoardrooms();

  Flux<BoardroomResponseDto> getLockedBoardrooms();

  Mono<BoardroomResponseDto> getBoardroomById(long boardroomId);

  Mono<LockedBoardroomResponseDto> getLockedBoardroomReasonById(long boardroomId);

  Flux<ReservationResponseDto> getBoardroomReservations(long boardroomId);

  Flux<EquipmentResponseDto> getBoardroomEquipments(long boardroomId);

  Flux<ReservationResponseDto> getBoardroomArchivedReservations(long boardroomId);

  Mono<UserResponseDto> getBoardroomAdministrator(long boardroomId);

  Flux<BoardroomContactResponseDto> getBoardroomContacts(long boardroomId);

  Mono<BoardroomResponseDto> createBoardroom(BoardroomDto boardroomDto);

  Mono<UserResponseDto> createBoardroomAdministrator(
      long boardroomId, BoardroomAdminDto boardroomAdminDto);

  Mono<BoardroomContactResponseDto> createBoardroomContact(
      long boardroomId, BoardroomContactDto boardroomContactDto);

  Mono<Void> removeBoardroomById(long boardroomId);

  Mono<Void> removeBoardroomContact(long boardroomId, long contactId);

  Mono<LockedBoardroomResponseDto> lockBoardroomById(
      long boardroomId, LockMessageDto lockMessageDto);

  Mono<Void> unLockBoardroomById(long boardroomId);

  Mono<BoardroomContactResponseDto> updateBoardroomContact(
      long boardroomId, long contactId, BoardroomContactDto boardroomContactDto);

  Mono<BoardroomResponseDto> updateBoardroomById(long boardroomId, BoardroomDto boardroomDto);

  Mono<ReservationOverlapResponseDto> checkBoardroomReservationOverlap(
      long boardroomId, ReservationEventDateDto reservationEventDateDto);

  Flux<BoardroomEventFilterResponseDto> filterAvailableBoardroomsByEventDate(
      ReservationEventDateDto reservationEventDateDto);
}
