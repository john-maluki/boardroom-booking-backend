package dev.johnmaluki.boardroom_booking_backend.boardroom.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockedBoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.mapper.BoardroomMapper;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.BoardroomRepository;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomService;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.service.CurrentUserService;
import dev.johnmaluki.boardroom_booking_backend.core.util.DataFilterUtil;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.mapper.EquipmentMapper;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.mapper.ReservationMapper;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardroomServiceImpl implements BoardroomService {
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    private final CurrentUserService currentUserService;
    private final BoardroomRepository boardroomRepository;
    private final BoardroomMapper boardroomMapper;
    private final EquipmentMapper equipmentMapper;
    private final ReservationMapper reservationMapper;
    @Override
    public List<BoardroomResponseDto> getAllBoardrooms() {
        return boardroomMapper.toBoardroomResponseDtoList(
                boardroomRepository.findByArchivedFalseAndDeletedFalse()
        );
    }

    @Override
    public BoardroomResponseDto getBoardroomById(long boardroomId) {
        return boardroomMapper.toBoardroomResponseDto(this.getBoardroomByIdFromDb(boardroomId));
    }

    @Override
    public LockedBoardroomResponseDto getLockedBoardroomReasonById(long boardroomId) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        List<LockedRoom> lockedRooms = new DataFilterUtil<LockedRoom>().removeArchivedAndDeletedRecords(
                boardroom.getLockedRooms()
        );
        if (lockedRooms.isEmpty()) {
            throw new ResourceNotFoundException(RESOURCE_NOT_FOUND);
        } else {
            return boardroomMapper.toLockedBoardroomResponseDto(lockedRooms.get(0));
        }
    }

    @Override
    public List<ReservationResponseDto> getBoardroomReservations(long boardroomId) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        return reservationMapper.toReservationResponseDtoList(
                this.filterReservationByUser(boardroom).stream()
                        .filter(reservation -> !reservation.getArchived() && !reservation.getDeleted())
                        .toList()
        );
    }

    @Override
    public List<EquipmentResponseDto> getBoardroomEquipments(long boardroomId) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        return equipmentMapper.toEquipmentResponseDtoList(boardroom.getEquipments());
    }

    private List<Reservation> filterReservationByUser(Boardroom boardroom) {
        long userId = currentUserService.getUserId();
        long boardroomAdminId = boardroom.getAdministrator().getId();
        if (currentUserService.getUserRole() == RoleType.ADMIN || boardroomAdminId == userId) {
            return boardroom.getReservations();
        } else {
            return boardroom.getReservations().stream().filter(
                    reservation -> reservation.getUser().getId() == userId
            ).toList();
        }
    }

    private Boardroom getBoardroomByIdFromDb(long boardroomId) {
        return boardroomRepository.findByIdAndArchivedFalseAndDeletedFalse(boardroomId).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND));
    }

}
