package dev.johnmaluki.boardroom_booking_backend.boardroom.service;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.*;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationOverlapResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;

import java.util.List;

public interface BoardroomService {
    List<BoardroomResponseDto> getAllBoardrooms();
    List<BoardroomResponseDto> getLockedBoardrooms();
    BoardroomResponseDto getBoardroomById(long boardroomId);
    LockedBoardroomResponseDto getLockedBoardroomReasonById(long boardroomId);
    List<ReservationResponseDto> getBoardroomReservations(long boardroomId);
    List<EquipmentResponseDto> getBoardroomEquipments(long boardroomId);
    List<ReservationResponseDto> getBoardroomArchivedReservations(long boardroomId);
    UserResponseDto getBoardroomAdministrator(long boardroomId);
    List<BoardroomContactResponseDto> getBoardroomContacts(long boardroomId);
    BoardroomResponseDto createBoardroom(BoardroomDto boardroomDto);
    UserResponseDto createBoardroomAdministrator(long boardroomId, BoardroomAdminDto boardroomAdminDto);
    BoardroomContactResponseDto createBoardroomContact(long boardroomId, BoardroomContactDto boardroomContactDto);
    void removeBoardroomById(long boardroomId);
    void removeBoardroomContact(long boardroomId, long contactId);
    LockedBoardroomResponseDto lockBoardroomById(long boardroomId, LockMessageDto lockMessageDto);
    void unLockBoardroomById(long boardroomId);
    BoardroomContactResponseDto updateBoardroomContact(long boardroomId, long contactId, BoardroomContactDto boardroomContactDto);
    BoardroomResponseDto updateBoardroomById(long boardroomId, BoardroomDto boardroomDto);
   ReservationOverlapResponseDto checkBoardroomReservationOverlap(long boardroomId, ReservationEventDateDto reservationEventDateDto);
}
