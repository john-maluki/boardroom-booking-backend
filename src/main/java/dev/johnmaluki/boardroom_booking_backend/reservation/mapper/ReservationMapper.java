package dev.johnmaluki.boardroom_booking_backend.reservation.mapper;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationOverlapResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.reservation.view.ReservationView;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.util.DateTimeUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps reservations to their response shape.
 *
 * <p>The viewer's timezone is now a parameter. It used to be read from {@code
 * SecurityContextHolder} inside the mapper, which is not available on a reactive stack and made the
 * mapper silently depend on being called from a request thread. Passing it in keeps the mapper a
 * pure function and lets a caller map a batch of rows after resolving the timezone once.
 */
@Component
@RequiredArgsConstructor
public class ReservationMapper {
  private final DateTimeUtil dateTimeUtil;

  /** Maps a row already joined to its boardroom and owner. */
  public ReservationResponseDto toReservationResponseDto(ReservationView view, String userTimeZone) {
    LocalDateTime start =
        dateTimeUtil.obtainLocalDateTimeBasedOnUserZone(view.startLocalDateTime(), userTimeZone);
    LocalDateTime end =
        dateTimeUtil.obtainLocalDateTimeBasedOnUserZone(view.endLocalDateTime(), userTimeZone);
    return ReservationResponseDto.builder()
        .id(view.id())
        .meetingTitle(view.meetingTitle())
        .meetingDescription(view.meetingDescription())
        .meetingType(view.meetingType())
        .approvalStatus(view.approvalStatus())
        .cancellationMessage(view.cancellationMessage())
        .attendees(view.attendees())
        .ictSupportRequired(view.ictSupportRequired())
        .isUrgentMeeting(view.urgentMeeting())
        .recordMeeting(view.recordMeeting())
        .meetingLink(view.meetingLink())
        .startDate(start.toLocalDate())
        .endDate(end.toLocalDate())
        .startTime(start.toLocalTime())
        .endTime(end.toLocalTime())
        .boardroomId(view.boardroomId())
        .userId(view.userId())
        .reservedBy(view.userFullName())
        .boardroomName(view.boardroomName())
        .ownerEmail(view.userEmail())
        .tag(view.tag())
        .build();
  }

  /**
   * Maps a freshly written reservation using the boardroom and user the caller already has in hand,
   * so a create or update does not need a second round trip just to render its own response.
   */
  public ReservationResponseDto toReservationResponseDto(
      Reservation reservation, Boardroom boardroom, AppUser user, String userTimeZone) {
    return toReservationResponseDto(toView(reservation, boardroom, user), userTimeZone);
  }

  public ReservationView toView(Reservation reservation, Boardroom boardroom, AppUser user) {
    return new ReservationView(
        reservation.getId(),
        reservation.getTag(),
        reservation.getStartLocalDateTime(),
        reservation.getEndLocalDateTime(),
        reservation.getApprovalStatus(),
        reservation.getCancellationMessage(),
        reservation.getMeetingTitle(),
        reservation.getMeetingDescription(),
        reservation.getMeetingType(),
        reservation.getAttendees(),
        reservation.isIctSupportRequired(),
        reservation.isUrgentMeeting(),
        reservation.isRecordMeeting(),
        reservation.getMeetingLink(),
        reservation.getCreatedAt(),
        boardroom.getId(),
        boardroom.getName(),
        boardroom.getEmail(),
        boardroom.getAdministratorId(),
        user.getId(),
        user.getFullName(),
        user.getEmail());
  }

  public Reservation toReservation(ReservationDto reservationDto) {
    return Reservation.builder()
        .meetingTitle(reservationDto.meetingTitle())
        .meetingType(reservationDto.meetingType())
        .meetingDescription(reservationDto.meetingDescription())
        .attendees(reservationDto.attendees())
        .ictSupportRequired(reservationDto.ictSupportRequired())
        .isUrgentMeeting(reservationDto.isUrgentMeeting())
        .recordMeeting(reservationDto.recordMeeting())
        .startLocalDateTime(
            dateTimeUtil.obtainLocalDateTimeFromISOString(reservationDto.startDateTime()))
        .endLocalDateTime(
            dateTimeUtil.obtainLocalDateTimeFromISOString(reservationDto.endDateTime()))
        .build();
  }

  public ReservationOverlapResponseDto toReservationOverlapResponseDto(boolean overlap) {
    return ReservationOverlapResponseDto.builder().overlap(overlap).build();
  }
}
