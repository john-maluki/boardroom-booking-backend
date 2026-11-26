package dev.johnmaluki.boardroom_booking_backend.reservation.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomServiceUtil;
import dev.johnmaluki.boardroom_booking_backend.core.exception.InvalidReservationWindowException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ReservationOverlapException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceOwnershipException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceUpdateExeption;
import dev.johnmaluki.boardroom_booking_backend.core.service.CurrentUserService;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ApproveReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ChangeVenueDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationMeetingLinkDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.mapper.ReservationMapper;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.notification.repository.ApplicationNotificationRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationQueryRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationNotifier;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationService;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationUtilService;
import dev.johnmaluki.boardroom_booking_backend.reservation.view.ReservationView;
import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import dev.johnmaluki.boardroom_booking_backend.util.DateTimeUtil;
import dev.johnmaluki.boardroom_booking_backend.util.EmailUtil;
import dev.johnmaluki.boardroom_booking_backend.util.MeetingType;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * Reservations.
 *
 * <p>Two things changed in the move to a reactive stack, beyond the signatures.
 *
 * <p>Access scoping is now done in SQL. Previously every listing loaded the whole table and then
 * dropped rows a non-administrator was not allowed to see; the repositories take a nullable user id
 * instead, so a regular user's query only ever touches their own rows.
 *
 * <p>Notifications and email have left the request path entirely — see {@link
 * ReservationNotifier}. A create no longer waits for an SMTP conversation before responding, and a
 * mail server that is down can no longer fail a booking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {
  private static final String NOT_FOUND = "Reservation could not be found";
  private static final String OVERLAP_MESSAGE =
      "Event overlaps with an existing one. Please choose another time.";
  private static final long DELETE_GRACE_MINUTES = 10;

  private final CurrentUserService currentUserService;
  private final ReservationRepository reservationRepository;
  private final ReservationQueryRepository reservationQueryRepository;
  private final ReservationMapper reservationMapper;
  private final BoardroomServiceUtil boardroomServiceUtil;
  private final DateTimeUtil dateTimeUtil;
  private final ReservationUtilService reservationUtilService;
  private final ReservationNotifier notifier;
  private final ApplicationNotificationRepository notificationRepository;

  @Override
  public Flux<ReservationResponseDto> getAllReservations() {
    return readContext()
        .flatMapMany(
            context ->
                reservationQueryRepository
                    .findAllActive(context.getT1().orElse(null))
                    .map(view -> toDto(view, context.getT2())));
  }

  @Override
  public Flux<ReservationResponseDto> getUpcomingReservations() {
    LocalDateTime now = dateTimeUtil.getCurrentLocalDateTimeUtc();
    return readContext()
        .flatMapMany(
            context ->
                reservationQueryRepository
                    .findUpcoming(context.getT1().orElse(null), now)
                    .map(view -> toDto(view, context.getT2())));
  }

  @Override
  public Flux<ReservationResponseDto> getAllLiveMeetings() {
    LocalDateTime now = dateTimeUtil.getCurrentLocalDateTimeUtc();
    return readContext()
        .flatMapMany(
            context ->
                reservationQueryRepository
                    .findLiveMeetings(context.getT1().orElse(null), now)
                    .map(view -> toDto(view, context.getT2())));
  }

  @Override
  public Flux<ReservationResponseDto> getArchivedReservations() {
    return readContext()
        .flatMapMany(
            context ->
                reservationQueryRepository
                    .findArchived(context.getT1().orElse(null))
                    .map(view -> toDto(view, context.getT2())));
  }

  @Override
  public Mono<ReservationResponseDto> getReservationById(long reservationId) {
    return findViewById(reservationId)
        .flatMap(view -> assertOwnership(view).thenReturn(view))
        .flatMap(this::toDtoForCurrentUser);
  }

  @Override
  @Transactional
  public Mono<ReservationResponseDto> createReservation(ReservationDto reservationDto) {
    ReservationEventDateDto window =
        ReservationEventDateDto.builder()
            .startDateTime(reservationDto.startDateTime())
            .endDateTime(reservationDto.endDateTime())
            .build();

    return assertValidWindow(window)
        .then(boardroomServiceUtil.findBoardroomById(reservationDto.boardroomId()))
        .flatMap(
            boardroom ->
                boardroomServiceUtil
                    .lockBoardroomForBooking(boardroom.getId())
                    .then(assertNoOverlap(null, boardroom.getId(), window))
                    .then(Mono.zip(currentUserService.getAppUser(), Mono.just(boardroom))))
        .flatMap(
            tuple -> {
              Boardroom boardroom = tuple.getT2();
              Reservation reservation = reservationMapper.toReservation(reservationDto);
              reservation.setBoardroomId(boardroom.getId());
              reservation.setUserId(tuple.getT1().getId());
              return reservationRepository
                  .save(reservation)
                  .map(saved -> reservationMapper.toView(saved, boardroom, tuple.getT1()));
            })
        .flatMap(
            view ->
                currentUserService
                    .getAppUserTimezone()
                    .flatMap(
                        timeZone ->
                            notifier
                                .requestApproval(view, timeZone)
                                .thenReturn(toDto(view, timeZone))));
  }

  @Override
  @Transactional
  public Mono<ReservationResponseDto> approveReservation(
      long reservationId, ApproveReservationDto approveReservationDto) {
    ApprovalStatus approvalStatus = approveReservationDto.approvalStatus();
    return findEntityById(reservationId)
        .flatMap(
            reservation -> {
              reservation.setApprovalStatus(approvalStatus);
              reservation.setCancellationMessage(
                  approvalStatus == ApprovalStatus.DECLINED
                      ? approveReservationDto.cancellationMessage()
                      : null);
              if (approvalStatus == ApprovalStatus.DECLINED) {
                return reservationRepository.save(reservation);
              }
              // Declined bookings don't hold the room, so its slot may have been taken since.
              // Accepting (or re-opening) one must not double-book the room.
              return boardroomServiceUtil
                  .lockBoardroomForBooking(reservation.getBoardroomId())
                  .then(boardroomServiceUtil.assertNoReservationOverlap(reservation))
                  .then(reservationRepository.save(reservation));
            })
        .flatMap(saved -> findViewById(saved.getId()))
        .flatMap(
            view ->
                currentUserService
                    .getAppUserTimezone()
                    .flatMap(
                        timeZone -> {
                          if (view.approvalStatus() != ApprovalStatus.APPROVED) {
                            return Mono.just(toDto(view, timeZone));
                          }
                          Mono<Void> announcements = notifier.announceApproval(view);
                          if (view.meetingType() != MeetingType.PHYSICAL) {
                            // A physical meeting needs no link, so nobody is asked to create one.
                            announcements =
                                announcements.then(notifier.requestMeetingLink(view, timeZone));
                          }
                          return announcements.thenReturn(toDto(view, timeZone));
                        }));
  }

  @Override
  @Transactional
  public Mono<ReservationResponseDto> updateReservationWithMeetingLink(
      long reservationId, ReservationMeetingLinkDto reservationMeetingLinkDto) {
    return findEntityById(reservationId)
        .flatMap(
            reservation -> {
              boolean wasUnset = reservation.getMeetingLink() == null;
              reservation.setMeetingLink(reservationMeetingLinkDto.meetingLink());
              return reservationRepository
                  .save(reservation)
                  .flatMap(saved -> findViewById(saved.getId()))
                  .zipWith(Mono.just(wasUnset));
            })
        .flatMap(
            tuple -> {
              ReservationView view = tuple.getT1();
              boolean firstLink = tuple.getT2();
              return currentUserService
                  .getAppUserTimezone()
                  .flatMap(
                      timeZone -> {
                        Mono<Void> announcement =
                            firstLink && view.meetingType() != MeetingType.PHYSICAL
                                ? notifier.announceMeetingLink(view, timeZone)
                                : notifier.announceDetailChange(
                                    view,
                                    timeZone,
                                    EmailUtil.SUBJECT_RESERVATION_MEETING_LINK_UPDATE);
                        return announcement.thenReturn(toDto(view, timeZone));
                      });
            });
  }

  @Override
  @Transactional
  public Mono<ReservationResponseDto> changeReservationVenue(
      long reservationId, ChangeVenueDto changeVenueDto) {
    return Mono.zip(
            findEntityById(reservationId),
            boardroomServiceUtil.findBoardroomById(changeVenueDto.boardroomId()))
        .flatMap(
            tuple -> {
              Reservation reservation = tuple.getT1();
              Boardroom boardroom = tuple.getT2();
              reservation.setBoardroomId(boardroom.getId());
              // Moving a booking sends it back for approval by the new boardroom's administrator.
              reservation.setApprovalStatus(ApprovalStatus.PENDING);
              reservation.setCancellationMessage(null);
              return boardroomServiceUtil
                  .lockBoardroomForBooking(boardroom.getId())
                  .then(boardroomServiceUtil.assertNoReservationOverlap(reservation))
                  .then(reservationRepository.save(reservation));
            })
        .flatMap(saved -> findViewById(saved.getId()))
        .flatMap(
            view ->
                currentUserService
                    .getAppUserTimezone()
                    .flatMap(
                        timeZone ->
                            notifier
                                .requestApproval(view, timeZone)
                                .thenReturn(toDto(view, timeZone))));
  }

  @Override
  @Transactional
  public Mono<ReservationResponseDto> rescheduleReservation(
      long reservationId, ReservationEventDateDto reservationEventDateDto) {
    LocalDateTime start =
        dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.startDateTime());
    LocalDateTime end =
        dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.endDateTime());

    return assertValidWindow(reservationEventDateDto)
        .then(findEntityById(reservationId))
        .flatMap(
            reservation ->
                boardroomServiceUtil
                    .lockBoardroomForBooking(reservation.getBoardroomId())
                    .then(
                        assertNoOverlap(
                            reservationId, reservation.getBoardroomId(), reservationEventDateDto))
                    .then(Mono.just(reservation)))
        .flatMap(
            reservation -> {
              reservation.setStartLocalDateTime(start);
              reservation.setEndLocalDateTime(end);
              return reservationRepository.save(reservation);
            })
        .flatMap(saved -> findViewById(saved.getId()))
        .flatMap(
            view ->
                currentUserService
                    .getAppUserTimezone()
                    .flatMap(
                        timeZone ->
                            notifier
                                .announceDetailChange(
                                    view, timeZone, EmailUtil.SUBJECT_RESERVATION_RESCHEDULED)
                                .thenReturn(toDto(view, timeZone))));
  }

  /**
   * A booking cancelled within ten minutes of being made is removed outright, on the assumption it
   * was a mistake; after that it is soft-deleted so the audit trail survives.
   */
  @Override
  @Transactional
  public Mono<Void> removeReservation(long reservationId) {
    return findEntityById(reservationId)
        .flatMap(
            reservation -> {
              if (createdWithinGracePeriod(reservation.getCreatedAt())) {
                // Notifications reference the reservation, so they go first.
                return notificationRepository
                    .deleteByReservationId(reservation.getId())
                    .then(reservationRepository.delete(reservation));
              }
              reservation.setDeleted(true);
              return reservationRepository.save(reservation).then();
            });
  }

  @Override
  @Transactional
  public Mono<ReservationResponseDto> updateReservation(
      long reservationId, ReservationDto reservationDto) {
    Reservation incoming = reservationMapper.toReservation(reservationDto);
    return findEntityById(reservationId)
        .flatMap(
            current -> {
              boolean anyMeetingUpdate =
                  reservationUtilService.checkAnyReservationUpdate(incoming, current);
              Set<String> newAttendees =
                  reservationUtilService.getNewAttendees(incoming, current);
              if (!anyMeetingUpdate && newAttendees.isEmpty()) {
                return Mono.<Reservation>error(
                    new ResourceUpdateExeption("No changes detected!"));
              }
              current.setMeetingTitle(incoming.getMeetingTitle());
              current.setMeetingDescription(incoming.getMeetingDescription());
              current.setMeetingType(incoming.getMeetingType());
              current.setAttendees(incoming.getAttendees());
              current.setIctSupportRequired(incoming.isIctSupportRequired());
              current.setRecordMeeting(incoming.isRecordMeeting());
              current.setUrgentMeeting(incoming.isUrgentMeeting());
              return reservationRepository.save(current);
            })
        .flatMap(saved -> findViewById(saved.getId()))
        .flatMap(this::toDtoForCurrentUser);
  }

  private boolean createdWithinGracePeriod(LocalDateTime createdAt) {
    if (createdAt == null) {
      return false;
    }
    return ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now()) <= DELETE_GRACE_MINUTES;
  }

  /**
   * A booking is visible to its owner, to the administrator of the boardroom it is in, and to any
   * system administrator.
   */
  private Mono<Void> assertOwnership(ReservationView view) {
    return currentUserService
        .getCurrentUser()
        .flatMap(
            principal -> {
              boolean allowed =
                  principal.isAdmin()
                      || principal.getUserId() == view.userId()
                      || (view.boardroomAdministratorId() != null
                          && principal.getUserId() == view.boardroomAdministratorId());
              return allowed
                  ? Mono.<Void>empty()
                  : Mono.error(new ResourceOwnershipException("Not allowed to access"));
            });
  }

  private Mono<Void> assertNoOverlap(
      Long reservationId, Long boardroomId, ReservationEventDateDto window) {
    LocalDateTime start = dateTimeUtil.obtainLocalDateTimeFromISOString(window.startDateTime());
    LocalDateTime end = dateTimeUtil.obtainLocalDateTimeFromISOString(window.endDateTime());
    return boardroomServiceUtil
        .findConflicts(reservationId, boardroomId, start, end)
        .collectList()
        .flatMap(
            conflicts ->
                conflicts.isEmpty()
                    ? Mono.<Void>empty()
                    : Mono.error(new ReservationOverlapException(OVERLAP_MESSAGE, conflicts)));
  }

  /** The end must come after the start; anything else can't be checked for clashes sensibly. */
  private Mono<Void> assertValidWindow(ReservationEventDateDto window) {
    return Mono.fromCallable(
            () -> {
              LocalDateTime start =
                  dateTimeUtil.obtainLocalDateTimeFromISOString(window.startDateTime());
              LocalDateTime end =
                  dateTimeUtil.obtainLocalDateTimeFromISOString(window.endDateTime());
              if (!end.isAfter(start)) {
                throw new InvalidReservationWindowException(
                    "The meeting must end after it starts.");
              }
              return true;
            })
        .then();
  }

  private Mono<Reservation> findEntityById(long reservationId) {
    return reservationRepository
        .findByIdAndArchivedFalseAndDeletedFalse(reservationId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(NOT_FOUND)));
  }

  private Mono<ReservationView> findViewById(long reservationId) {
    return reservationQueryRepository
        .findViewById(reservationId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(NOT_FOUND)));
  }

  private ReservationResponseDto toDto(ReservationView view, String timeZone) {
    return reservationMapper.toReservationResponseDto(view, timeZone);
  }

  private Mono<ReservationResponseDto> toDtoForCurrentUser(ReservationView view) {
    return currentUserService.getAppUserTimezone().map(timeZone -> toDto(view, timeZone));
  }

  /** The acting user's listing filter and timezone, resolved once per request. */
  private Mono<Tuple2<Optional<Long>, String>> readContext() {
    return Mono.zip(
        currentUserService.getReadFilterUserId(), currentUserService.getAppUserTimezone());
  }
}
