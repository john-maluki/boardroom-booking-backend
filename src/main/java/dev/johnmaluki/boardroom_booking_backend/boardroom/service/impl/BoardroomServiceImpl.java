package dev.johnmaluki.boardroom_booking_backend.boardroom.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.*;
import dev.johnmaluki.boardroom_booking_backend.boardroom.mapper.BoardroomContactMapper;
import dev.johnmaluki.boardroom_booking_backend.boardroom.mapper.BoardroomMapper;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.BoardroomContact;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.BoardroomContactRepository;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.BoardroomRepository;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.LockedRoomRepository;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomService;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomServiceUtil;
import dev.johnmaluki.boardroom_booking_backend.core.exception.DuplicateResourceException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.service.CurrentUserService;
import dev.johnmaluki.boardroom_booking_backend.core.service.FileService;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.mapper.EquipmentMapper;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationOverlapResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.mapper.ReservationMapper;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationRepository;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.mapper.UserMapper;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.util.DateTimeUtil;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardroomServiceImpl implements BoardroomService, BoardroomServiceUtil {
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    private final CurrentUserService currentUserService;
    private final BoardroomRepository boardroomRepository;
    private final ReservationRepository reservationRepository;
    private final AppUserRepository userRepository;
    private final LockedRoomRepository lockedRoomRepository;
    private final BoardroomContactRepository boardroomContactRepository;
    private final BoardroomMapper boardroomMapper;
    private final EquipmentMapper equipmentMapper;
    private final UserMapper userMapper;
    private final BoardroomContactMapper boardroomContactMapper;
    private final ReservationMapper reservationMapper;
    private final DateTimeUtil dateTimeUtil;
    private final FileService fileService;
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
    public List<BoardroomResponseDto> getLockedBoardrooms() {
        List<Boardroom> lockedBoardrooms = boardroomRepository.findAllByLockedTrueAndDeletedFalseAndArchivedFalse();
        return boardroomMapper.toBoardroomResponseDtoList(lockedBoardrooms);
    }

    @Override
    public BoardroomResponseDto getBoardroomById(long boardroomId) {
        Map<Long, Boolean> boardroomOnGoingMeetingStatuses = this.getOngoingMeetingStatuses();
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        boardroom.setBoardroomContacts(
                boardroom.getBoardroomContacts().stream().filter(
                        contact -> !contact.getArchived() && !contact.getDeleted()
                ).toList()
        );
        boardroom.setHasOngoingMeeting(boardroomOnGoingMeetingStatuses.getOrDefault(
                boardroom.getId(), false
        ));
        return boardroomMapper.toBoardroomResponseDto(boardroom);
    }

    @Override
    public LockedBoardroomResponseDto getLockedBoardroomReasonById(long boardroomId) {
        LockedRoom lockedRoom = this.getLockedRoomByBoardroomId(boardroomId);
        return boardroomMapper.toLockedBoardroomResponseDto(lockedRoom);

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
        if(boardroom.getEquipments().isEmpty()) {
            return Collections.emptyList();
        }
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
        AppUser user = Optional.ofNullable(boardroom.getAdministrator()).orElseThrow(
                () -> new ResourceNotFoundException("Boardroom contains no administrator")
        );
        return userMapper.toUserResponseDto(user);
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

    @Override
    public BoardroomResponseDto createBoardroom(BoardroomDto boardroomDto) {
        try {
            Boardroom boardroom = boardroomMapper.toBoardroom(boardroomDto);
            return boardroomMapper.toBoardroomResponseDto(
                    boardroomRepository.save(boardroom)
            );
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Resource already exists.");
        }
    }

    @Override
    @Transactional
    public UserResponseDto createBoardroomAdministrator(long boardroomId, BoardroomAdminDto boardroomAdminDto) {
        try {
            Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
            AppUser user = this.getAppUserByIdFromDb(boardroomAdminDto.userId());
            boardroom.setAdministrator(user);
            user.setBoardroom(boardroom);
            return userMapper.toUserResponseDto(boardroomRepository.save(boardroom).getAdministrator());
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Resource already exists.");
        }
    }

    @Override
    public BoardroomContactResponseDto createBoardroomContact(long boardroomId, BoardroomContactDto boardroomContactDto) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        BoardroomContact boardroomContact = boardroomContactMapper.toBoardroomContact(boardroomContactDto);
        boardroom.addBoardroomContact(boardroomContact);
        boardroomContact = boardroomContactRepository.save(boardroomContact);
        return boardroomContactMapper.toBoardroomContactResponseDto(boardroomContact);
    }

    @Override
    public void removeBoardroomById(long boardroomId) {
        this.deleteBoardroomSoftly(boardroomId);
    }

    @Override
    public void removeBoardroomContact(long boardroomId, long contactId) {
        this.deleteBoardroomContactSoftly(boardroomId, contactId);
    }

    @Override
    public LockedBoardroomResponseDto lockBoardroomById(long boardroomId, LockMessageDto lockMessageDto) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        LockedRoom lockedRoom = LockedRoom.builder()
                .locked(true)
                .givenReason(lockMessageDto.givenReason())
                .build();
        boardroom.setLocked(true);
        LockedRoom savedLockedRoom = lockedRoomRepository.getByBoardroomAndLockedTrueAndArchivedFalseAndDeletedFalse(boardroom)
                .orElse(lockedRoom);
        savedLockedRoom.setGivenReason(lockMessageDto.givenReason());
        boardroom.addLockedBoardroom(savedLockedRoom);
        return boardroomMapper.toLockedBoardroomResponseDto(
                lockedRoomRepository.save(savedLockedRoom)
        );
    }

    @Override
    public void unLockBoardroomById(long boardroomId) {
        LockedRoom lockedRoom = this.getLockedRoomByBoardroomId(boardroomId);
        lockedRoom.setLocked(false);
        lockedRoom.getBoardroom().setLocked(false);
        lockedRoomRepository.save(lockedRoom);
    }

    @Override
    public BoardroomContactResponseDto updateBoardroomContact(
            long boardroomId,
            long contactId,
            BoardroomContactDto boardroomContactDto
    ) {
        BoardroomContact boardroomContact = boardroomContactRepository.getContactRecord(contactId, boardroomId);
        boardroomContact.setContact(boardroomContactDto.contact());
        return boardroomContactMapper.toBoardroomContactResponseDto(
                boardroomContactRepository.save(boardroomContact)
        );
    }

    @Override
    public BoardroomResponseDto updateBoardroomById(long boardroomId, BoardroomDto boardroomDto) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        boardroom.setName(boardroomDto.name());
        boardroom.setCentre(boardroomDto.centre());
        boardroom.setDepartment(boardroomDto.department());
        boardroom.setCapacity(boardroomDto.capacity());
        boardroom.setDescription(boardroomDto.description());
        boardroom.setInternetEnabled(boardroomDto.internetEnabled());
        boardroom.setEmail(boardroomDto.email());
        boardroom.setMeetingTypeSupported(boardroomDto.meetingTypeSupported());
        boardroom.setPicture(boardroomDto.picture());
        return boardroomMapper.toBoardroomResponseDto(boardroomRepository.save(boardroom)
        );
    }

    @Override
    public ReservationOverlapResponseDto checkBoardroomReservationOverlap(long boardroomId, ReservationEventDateDto reservationEventDateDto) {
        LocalDateTime startLocalDateTime = dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.startDateTime());
        LocalDateTime endLocalDateTime = dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.endDateTime());
        List<Reservation> conflictingEvents = reservationRepository.findConflictingEvents(
                boardroomId,
                startLocalDateTime,
                endLocalDateTime
        );
        return reservationMapper.toReservationOverlapResponseDto(!conflictingEvents.isEmpty());

    }

    private List<Reservation> filterReservationByUser(Boardroom boardroom) {
        if(boardroom.getReservations().isEmpty()) {
            return Collections.emptyList();
        }
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
        LocalDateTime currentDateTime = dateTimeUtil.getCurrentLocalDateTimeUtc();
        List<Object[]> ongoingMeetingStatus = reservationRepository.findBoardroomOngoingMeetingStatus(currentDateTime);
        return ongoingMeetingStatus.stream()
                .collect(Collectors.toMap(
                        status -> (Long) status[0],
                        status -> (Boolean) status[1]
                ));
    }

    private AppUser getAppUserByIdFromDb(long userId) {
        return userRepository.findByIdAndArchivedFalseAndDeletedFalse(userId).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND));
    }

    @Override
    public Boardroom findBoardroomById(long boardroomId) {
        return this.getBoardroomByIdFromDb(boardroomId);
    }

    private void deleteBoardroomSoftly(long boardroomId) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        boardroom.setDeleted(true);
        boardroomRepository.save(boardroom);
    }

    private void deleteBoardroomContactSoftly(long boardroomId, long contactId) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        BoardroomContact boardroomContact = boardroom.getBoardroomContacts().stream()
                        .filter( contact -> contact.getId() == contactId)
                .toList().get(0);
        boolean isDeleted = boardroomContact.getDeleted();
        if (isDeleted) {
            throw new ResourceNotFoundException("Contact not found");
        } else {
            boardroomContact.setDeleted(true);
            boardroomRepository.save(boardroom);
        }

    }

    private LockedRoom getLockedRoomByBoardroomId(long boardroomId) {
        Boardroom boardroom = this.getBoardroomByIdFromDb(boardroomId);
        return lockedRoomRepository.getByBoardroomAndLockedTrueAndArchivedFalseAndDeletedFalse(boardroom)
                .orElseThrow(() -> new ResourceNotFoundException("Locked room not found"));
    }
}
