package dev.johnmaluki.boardroom_booking_backend.boardroom.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockedBoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.mapper.BoardroomContactMapper;
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
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationRepository;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.mapper.UserMapper;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardroomServiceImpl implements BoardroomService {
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    private final CurrentUserService currentUserService;
    private final BoardroomRepository boardroomRepository;
    private final ReservationRepository reservationRepository;
    private final BoardroomMapper boardroomMapper;
    private final EquipmentMapper equipmentMapper;
    private final UserMapper userMapper;
    private final BoardroomContactMapper boardroomContactMapper;
    private final ReservationMapper reservationMapper;
    @Override
    public List<BoardroomResponseDto> getAllBoardrooms() {
        Map<Long, Boolean> boardroomOnGoingMeetingStatuses = this.getOngoingMeetingStatuses();
        List<Boardroom> boardrooms = boardroomRepository.findByArchivedFalseAndDeletedFalse();
        for (Boardroom boardroom : boardrooms) {
            boardroom.setHasOngoingMeeting(boardroomOnGoingMeetingStatuses.getOrDefault(
                    boardroom.getId(), false
            ));
        }
        return boardroomMapper.toBoardroomResponseDtoList(boardrooms);
    }

    @Override
    public BoardroomResponseDto getBoardroomById(long boardroomId) {
        Map<Long, Boolean> boardroomOnGoingMeetingStatuses = this.getOngoingMeetingStatuses();
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        boardroom.setHasOngoingMeeting(boardroomOnGoingMeetingStatuses.getOrDefault(
                boardroom.getId(), false
        ));
        return boardroomMapper.toBoardroomResponseDto(boardroom);
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

    @Override
    public List<ReservationResponseDto> getBoardroomArchivedReservations(long boardroomId) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        return reservationMapper.toReservationResponseDtoList(
                this.filterReservationByUser(boardroom).stream()
                        .filter(reservation -> reservation.getArchived() && !reservation.getDeleted())
                        .toList()
        );
    }

    @Override
    public UserResponseDto getBoardroomAdministrator(long boardroomId) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        return userMapper.toUserResponseDto(boardroom.getAdministrator());
    }

    @Override
    public List<BoardroomContactResponseDto> getBoardroomContacts(long boardroomId) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        return boardroomContactMapper.toBoardroomContactResponseDtoList(
                boardroom.getBoardroomContacts().stream()
                        .filter(boardroomContact -> !boardroomContact.getArchived() && !boardroomContact.getDeleted())
                        .toList()
        );
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

    private Map<Long, Boolean> getOngoingMeetingStatuses() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        List<Object[]> ongoingMeetingStatus = reservationRepository.findBoardroomOngoingMeetingStatus(currentDate, currentTime);
        return ongoingMeetingStatus.stream()
                .collect(Collectors.toMap(
                        status -> (Long) status[0],
                        status -> (Boolean) status[1]
                ));
    }

}
