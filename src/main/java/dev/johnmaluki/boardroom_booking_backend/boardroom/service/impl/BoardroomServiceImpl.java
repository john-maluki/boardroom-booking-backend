package dev.johnmaluki.boardroom_booking_backend.boardroom.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomAdminDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomEventFilterResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockMessageDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockedBoardroomResponseDto;
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
import dev.johnmaluki.boardroom_booking_backend.boardroom.view.BoardroomMeetingStatus;
import dev.johnmaluki.boardroom_booking_backend.core.cache.CacheKeys;
import dev.johnmaluki.boardroom_booking_backend.core.cache.CacheTtl;
import dev.johnmaluki.boardroom_booking_backend.core.cache.ReactiveCacheService;
import dev.johnmaluki.boardroom_booking_backend.core.exception.DuplicateResourceException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ReservationOverlapException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.service.CurrentUserService;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.mapper.EquipmentMapper;
import dev.johnmaluki.boardroom_booking_backend.equipment.repository.EquipmentRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationConflictDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationOverlapResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.mapper.ReservationMapper;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationQueryRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationRepository;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.mapper.UserMapper;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.UserQueryRepository;
import dev.johnmaluki.boardroom_booking_backend.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * Boardrooms, their contacts, their equipment and their locks.
 *
 * <p>Caching note: boardroom rows are cached, but the assembled {@code BoardroomResponseDto} is
 * not. The DTO carries {@code hasOngoingMeeting}, which is true only for the length of a meeting,
 * so caching the assembled response would serve a stale "in use" badge for the whole TTL. Instead
 * the rows come from Redis and the ongoing-meeting flag is recomputed per request by one grouped
 * query — the cheap part stays fresh and the repeated part stays cached.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BoardroomServiceImpl implements BoardroomService, BoardroomServiceUtil {
  public static final String RESOURCE_NOT_FOUND = "Resource not found";
  private static final String OVERLAP_MESSAGE =
      "Event overlaps with an existing one. Please choose another time.";

  private final CurrentUserService currentUserService;
  private final BoardroomRepository boardroomRepository;
  private final ReservationRepository reservationRepository;
  private final ReservationQueryRepository reservationQueryRepository;
  private final AppUserRepository userRepository;
  private final UserQueryRepository userQueryRepository;
  private final LockedRoomRepository lockedRoomRepository;
  private final BoardroomContactRepository boardroomContactRepository;
  private final EquipmentRepository equipmentRepository;
  private final BoardroomMapper boardroomMapper;
  private final EquipmentMapper equipmentMapper;
  private final UserMapper userMapper;
  private final BoardroomContactMapper boardroomContactMapper;
  private final ReservationMapper reservationMapper;
  private final DateTimeUtil dateTimeUtil;
  private final ReactiveCacheService cacheService;

  @Override
  public Flux<BoardroomResponseDto> getAllBoardrooms() {
    return Mono.zip(cachedActiveBoardrooms().collectList(), ongoingMeetingStatuses())
        .flatMapMany(tuple -> withOngoingStatus(tuple.getT1(), tuple.getT2()));
  }

  @Override
  public Flux<BoardroomResponseDto> getLockedBoardrooms() {
    return cacheService
        .cacheList(
            CacheKeys.BOARDROOM_LOCKED,
            CacheTtl.REFERENCE_DATA,
            Boardroom.class,
            boardroomRepository.findAllByLockedTrueAndDeletedFalseAndArchivedFalse())
        .map(boardroomMapper::toBoardroomResponseDto);
  }

  @Override
  public Mono<BoardroomResponseDto> getBoardroomById(long boardroomId) {
    return Mono.zip(
            cachedBoardroom(boardroomId),
            cachedContacts(boardroomId).collectList(),
            ongoingMeetingStatuses())
        .map(
            tuple -> {
              Boardroom boardroom = tuple.getT1();
              boardroom.setHasOngoingMeeting(
                  tuple.getT3().getOrDefault(boardroom.getId(), false));
              return boardroomMapper.toBoardroomResponseDto(boardroom, tuple.getT2());
            });
  }

  @Override
  public Mono<LockedBoardroomResponseDto> getLockedBoardroomReasonById(long boardroomId) {
    return getLockedRoomByBoardroomId(boardroomId).map(boardroomMapper::toLockedBoardroomResponseDto);
  }

  @Override
  public Flux<ReservationResponseDto> getBoardroomReservations(long boardroomId) {
    return findBoardroomById(boardroomId)
        .then(readContext())
        .flatMapMany(
            context ->
                reservationQueryRepository
                    .findActiveByBoardroom(boardroomId, context.getT1().orElse(null))
                    .map(view -> reservationMapper.toReservationResponseDto(view, context.getT2())));
  }

  @Override
  public Flux<ReservationResponseDto> getBoardroomArchivedReservations(long boardroomId) {
    return findBoardroomById(boardroomId)
        .then(readContext())
        .flatMapMany(
            context ->
                reservationQueryRepository
                    .findArchivedByBoardroom(boardroomId, context.getT1().orElse(null))
                    .map(view -> reservationMapper.toReservationResponseDto(view, context.getT2())));
  }

  @Override
  public Flux<EquipmentResponseDto> getBoardroomEquipments(long boardroomId) {
    return findBoardroomById(boardroomId)
        .thenMany(
            cacheService.cacheList(
                CacheKeys.boardroomEquipment(boardroomId),
                CacheTtl.REFERENCE_DATA,
                EquipmentResponseDto.class,
                equipmentRepository
                    .findByBoardroomIdAndArchivedFalseAndDeletedFalse(boardroomId)
                    .map(equipmentMapper::toEquipmentResponseDto)));
  }

  @Override
  public Mono<UserResponseDto> getBoardroomAdministrator(long boardroomId) {
    return findBoardroomById(boardroomId)
        .flatMap(
            boardroom ->
                boardroom.getAdministratorId() == null
                    ? Mono.<UserResponseDto>error(
                        new ResourceNotFoundException("Boardroom contains no administrator"))
                    : userQueryRepository
                        .findActiveById(boardroom.getAdministratorId())
                        .switchIfEmpty(
                            Mono.error(
                                new ResourceNotFoundException(
                                    "Boardroom contains no administrator")))
                        .map(userMapper::toUserResponseDto));
  }

  @Override
  public Flux<BoardroomContactResponseDto> getBoardroomContacts(long boardroomId) {
    return findBoardroomById(boardroomId).thenMany(cachedContacts(boardroomId));
  }

  @Override
  @Transactional
  public Mono<BoardroomResponseDto> createBoardroom(BoardroomDto boardroomDto) {
    return boardroomRepository
        .save(boardroomMapper.toBoardroom(boardroomDto))
        .onErrorMap(
            DataIntegrityViolationException.class,
            e -> new DuplicateResourceException("Resource already exists."))
        .map(boardroomMapper::toBoardroomResponseDto)
        .flatMap(dto -> evictBoardroomCaches().thenReturn(dto));
  }

  @Override
  @Transactional
  public Mono<UserResponseDto> createBoardroomAdministrator(
      long boardroomId, BoardroomAdminDto boardroomAdminDto) {
    return Mono.zip(findBoardroomById(boardroomId), findUserById(boardroomAdminDto.userId()))
        .flatMap(
            tuple -> {
              Boardroom boardroom = tuple.getT1();
              boardroom.setAdministratorId(tuple.getT2().getId());
              return boardroomRepository.save(boardroom);
            })
        .onErrorMap(
            DataIntegrityViolationException.class,
            e -> new DuplicateResourceException("Resource already exists."))
        .flatMap(saved -> userQueryRepository.findActiveById(saved.getAdministratorId()))
        .map(userMapper::toUserResponseDto)
        .flatMap(dto -> evictBoardroomCaches().thenReturn(dto));
  }

  @Override
  @Transactional
  public Mono<BoardroomContactResponseDto> createBoardroomContact(
      long boardroomId, BoardroomContactDto boardroomContactDto) {
    return findBoardroomById(boardroomId)
        .flatMap(
            boardroom -> {
              BoardroomContact contact =
                  boardroomContactMapper.toBoardroomContact(boardroomContactDto);
              contact.setBoardroomId(boardroom.getId());
              return boardroomContactRepository.save(contact);
            })
        .map(boardroomContactMapper::toBoardroomContactResponseDto)
        .flatMap(dto -> evictBoardroomCaches().thenReturn(dto));
  }

  @Override
  @Transactional
  public Mono<Void> removeBoardroomById(long boardroomId) {
    return findBoardroomById(boardroomId)
        .flatMap(
            boardroom -> {
              boardroom.setDeleted(true);
              return boardroomRepository.save(boardroom);
            })
        .then(evictBoardroomCaches());
  }

  @Override
  @Transactional
  public Mono<Void> removeBoardroomContact(long boardroomId, long contactId) {
    return boardroomContactRepository
        .findByIdAndBoardroomIdAndArchivedFalseAndDeletedFalse(contactId, boardroomId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Contact not found")))
        .flatMap(
            contact -> {
              contact.setDeleted(true);
              return boardroomContactRepository.save(contact);
            })
        .then(evictBoardroomCaches());
  }

  @Override
  @Transactional
  public Mono<BoardroomContactResponseDto> updateBoardroomContact(
      long boardroomId, long contactId, BoardroomContactDto boardroomContactDto) {
    return boardroomContactRepository
        .findByIdAndBoardroomIdAndArchivedFalseAndDeletedFalse(contactId, boardroomId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Contact not found")))
        .flatMap(
            contact -> {
              contact.setContact(boardroomContactDto.contact());
              return boardroomContactRepository.save(contact);
            })
        .map(boardroomContactMapper::toBoardroomContactResponseDto)
        .flatMap(dto -> evictBoardroomCaches().thenReturn(dto));
  }

  @Override
  @Transactional
  public Mono<BoardroomResponseDto> updateBoardroomById(
      long boardroomId, BoardroomDto boardroomDto) {
    return findBoardroomById(boardroomId)
        .flatMap(
            boardroom -> {
              boardroom.setName(boardroomDto.name());
              boardroom.setCentre(boardroomDto.centre());
              boardroom.setDepartment(boardroomDto.department());
              boardroom.setCapacity(boardroomDto.capacity());
              boardroom.setDescription(boardroomDto.description());
              boardroom.setInternetEnabled(boardroomDto.internetEnabled());
              boardroom.setEmail(boardroomDto.email());
              boardroom.setMeetingTypeSupported(boardroomDto.meetingTypeSupported());
              boardroom.setPicture(boardroomDto.picture());
              return boardroomRepository.save(boardroom);
            })
        .map(boardroomMapper::toBoardroomResponseDto)
        .flatMap(dto -> evictBoardroomCaches().thenReturn(dto));
  }

  @Override
  @Transactional
  public Mono<LockedBoardroomResponseDto> lockBoardroomById(
      long boardroomId, LockMessageDto lockMessageDto) {
    return findBoardroomById(boardroomId)
        .flatMap(
            boardroom -> {
              boardroom.setLocked(true);
              return boardroomRepository.save(boardroom);
            })
        .flatMap(
            boardroom ->
                lockedRoomRepository
                    .findByBoardroomIdAndLockedTrueAndArchivedFalseAndDeletedFalse(
                        boardroom.getId())
                    .defaultIfEmpty(
                        LockedRoom.builder().boardroomId(boardroom.getId()).locked(true).build()))
        .flatMap(
            lockedRoom -> {
              lockedRoom.setGivenReason(lockMessageDto.givenReason());
              return lockedRoomRepository.save(lockedRoom);
            })
        .map(boardroomMapper::toLockedBoardroomResponseDto)
        .flatMap(dto -> evictBoardroomCaches().thenReturn(dto));
  }

  @Override
  @Transactional
  public Mono<Void> unLockBoardroomById(long boardroomId) {
    return getLockedRoomByBoardroomId(boardroomId)
        .flatMap(
            lockedRoom -> {
              lockedRoom.setLocked(false);
              return lockedRoomRepository.save(lockedRoom);
            })
        .flatMap(lockedRoom -> findBoardroomById(lockedRoom.getBoardroomId()))
        .flatMap(
            boardroom -> {
              boardroom.setLocked(false);
              return boardroomRepository.save(boardroom);
            })
        .then(evictBoardroomCaches());
  }

  @Override
  public Mono<ReservationOverlapResponseDto> checkBoardroomReservationOverlap(
      long boardroomId, ReservationEventDateDto reservationEventDateDto, Long excludeReservationId) {
    LocalDateTime start =
        dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.startDateTime());
    LocalDateTime end =
        dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.endDateTime());
    return findConflicts(excludeReservationId, boardroomId, start, end)
        .collectList()
        .map(
            conflicts ->
                ReservationOverlapResponseDto.builder()
                    .overlap(!conflicts.isEmpty())
                    .conflicts(conflicts)
                    .build());
  }

  /**
   * Returns the boardrooms that already have a clashing booking in the requested window.
   *
   * <p>The behaviour is carried over verbatim from the previous implementation, including the
   * mismatch between the endpoint's name and what it returns. The distinct-boardroom step that was
   * done in Java has moved into the SQL.
   */
  @Override
  public Flux<BoardroomEventFilterResponseDto> filterAvailableBoardroomsByEventDate(
      ReservationEventDateDto reservationEventDateDto) {
    LocalDateTime start =
        dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.startDateTime());
    LocalDateTime end =
        dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.endDateTime());
    return reservationRepository
        .findBoardroomsWithConflictingEvents(start, end)
        .map(boardroomMapper::toBoardroomEventFilterResponseDto);
  }

  @Override
  public Mono<Boardroom> findBoardroomById(long boardroomId) {
    return boardroomRepository
        .findByIdAndArchivedFalseAndDeletedFalse(boardroomId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NOT_FOUND)));
  }

  @Override
  public Mono<Boolean> hasReservationOverlap(
      Long reservationId, Long boardroomId, ReservationEventDateDto reservationEventDateDto) {
    LocalDateTime start =
        dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.startDateTime());
    LocalDateTime end =
        dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.endDateTime());
    return reservationRepository
        .countBoardroomConflictingEvents(reservationId, boardroomId, start, end)
        .defaultIfEmpty(0L)
        .map(count -> count > 0);
  }

  @Override
  public Mono<Void> assertNoReservationOverlap(Reservation reservation) {
    return findConflicts(
            reservation.getId(),
            reservation.getBoardroomId(),
            reservation.getStartLocalDateTime(),
            reservation.getEndLocalDateTime())
        .collectList()
        .flatMap(
            conflicts ->
                conflicts.isEmpty()
                    ? Mono.<Void>empty()
                    : Mono.error(new ReservationOverlapException(OVERLAP_MESSAGE, conflicts)));
  }

  @Override
  public Flux<ReservationConflictDto> findConflicts(
      Long reservationId, Long boardroomId, LocalDateTime start, LocalDateTime end) {
    return reservationRepository
        .findBoardroomConflictingEvents(reservationId, boardroomId, start, end)
        .map(
            r ->
                ReservationConflictDto.builder()
                    .id(r.getId())
                    .meetingTitle(r.getMeetingTitle())
                    .startDateTime(r.getStartLocalDateTime().atOffset(ZoneOffset.UTC).toString())
                    .endDateTime(r.getEndLocalDateTime().atOffset(ZoneOffset.UTC).toString())
                    .approvalStatus(r.getApprovalStatus())
                    .build());
  }

  @Override
  public Mono<Void> lockBoardroomForBooking(long boardroomId) {
    return reservationRepository.lockBoardroomForBooking(boardroomId).then();
  }

  private Flux<Boardroom> cachedActiveBoardrooms() {
    return cacheService.cacheList(
        CacheKeys.BOARDROOM_ALL,
        CacheTtl.REFERENCE_DATA,
        Boardroom.class,
        boardroomRepository.findByArchivedFalseAndDeletedFalse());
  }

  private Mono<Boardroom> cachedBoardroom(long boardroomId) {
    return cacheService
        .cache(
            CacheKeys.boardroomById(boardroomId),
            CacheTtl.REFERENCE_DATA,
            Boardroom.class,
            boardroomRepository.findByIdAndArchivedFalseAndDeletedFalse(boardroomId))
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NOT_FOUND)));
  }

  private Flux<BoardroomContactResponseDto> cachedContacts(long boardroomId) {
    return cacheService.cacheList(
        CacheKeys.boardroomContacts(boardroomId),
        CacheTtl.REFERENCE_DATA,
        BoardroomContactResponseDto.class,
        boardroomContactRepository
            .findByBoardroomIdAndArchivedFalseAndDeletedFalse(boardroomId)
            .map(boardroomContactMapper::toBoardroomContactResponseDto));
  }

  private Mono<Map<Long, Boolean>> ongoingMeetingStatuses() {
    return reservationQueryRepository
        .findBoardroomOngoingMeetingStatus(dateTimeUtil.getCurrentLocalDateTimeUtc())
        .collect(
            Collectors.toMap(
                BoardroomMeetingStatus::boardroomId, BoardroomMeetingStatus::hasOngoingMeeting));
  }

  private Flux<BoardroomResponseDto> withOngoingStatus(
      List<Boardroom> boardrooms, Map<Long, Boolean> statuses) {
    return Flux.fromIterable(boardrooms)
        .map(
            boardroom -> {
              boardroom.setHasOngoingMeeting(statuses.getOrDefault(boardroom.getId(), false));
              return boardroomMapper.toBoardroomResponseDto(boardroom);
            });
  }

  /** The acting user's listing filter and timezone, resolved once per request. */
  private Mono<Tuple2<java.util.Optional<Long>, String>> readContext() {
    return Mono.zip(
        currentUserService.getReadFilterUserId(), currentUserService.getAppUserTimezone());
  }

  private Mono<dev.johnmaluki.boardroom_booking_backend.user.model.AppUser> findUserById(
      long userId) {
    return userRepository
        .findByIdAndArchivedFalseAndDeletedFalse(userId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(RESOURCE_NOT_FOUND)));
  }

  private Mono<LockedRoom> getLockedRoomByBoardroomId(long boardroomId) {
    return findBoardroomById(boardroomId)
        .flatMap(
            boardroom ->
                lockedRoomRepository.findByBoardroomIdAndLockedTrueAndArchivedFalseAndDeletedFalse(
                    boardroom.getId()))
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Locked room not found")));
  }

  private Mono<Void> evictBoardroomCaches() {
    return cacheService.evictByPattern(CacheKeys.BOARDROOM_NAMESPACE);
  }

}
