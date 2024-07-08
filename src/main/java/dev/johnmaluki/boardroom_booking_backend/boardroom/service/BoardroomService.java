package dev.johnmaluki.boardroom_booking_backend.boardroom.service;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockedBoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;

import java.util.List;

public interface BoardroomService {
    List<BoardroomResponseDto> getAllBoardrooms();
    BoardroomResponseDto getBoardroomById(long boardroomId);
    LockedBoardroomResponseDto getLockedBoardroomReasonById(long boardroomId);
    List<ReservationResponseDto> getBoardroomReservations(long boardroomId);
    List<EquipmentResponseDto> getBoardroomEquipments(long boardroomId);
    List<ReservationResponseDto> getBoardroomArchivedReservations(long boardroomId);
    UserResponseDto getBoardroomAdministrator(long boardroomId);
    List<BoardroomContactResponseDto> getBoardroomContacts(long boardroomId);
}
