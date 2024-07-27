package dev.johnmaluki.boardroom_booking_backend.reservation.mapper;


import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationMapper {
    private final DateTimeUtil dateTimeUtil;
    public ReservationResponseDto toReservationResponseDto(Reservation reservation) {
        String userTimeZone = this.getUserTimeZone();
        LocalDateTime startDateTime = reservation.getStartLocalDateTime();
        LocalDateTime endDateTime = reservation.getEndLocalDateTime();
        LocalDateTime localDateTimeStart = dateTimeUtil.obtainLocalDateTimeBasedOnUserZone(startDateTime, userTimeZone);
        LocalDateTime localDateTimeEnd = dateTimeUtil.obtainLocalDateTimeBasedOnUserZone(endDateTime, userTimeZone);
        return ReservationResponseDto.builder()
                .id(reservation.getId())
                .meetingTitle(reservation.getMeetingTitle())
                .meetingDescription(reservation.getMeetingDescription())
                .meetingType(reservation.getMeetingType())
                .approvalStatus(reservation.getApprovalStatus())
                .attendees(reservation.getAttendees())
                .ictSupportRequired(reservation.isIctSupportRequired())
                .isUrgentMeeting(reservation.isUrgentMeeting())
                .meetingLink(reservation.getMeetingLink())
                .startDate(localDateTimeStart.toLocalDate())
                .endDate(localDateTimeEnd.toLocalDate())
                .startTime(localDateTimeStart.toLocalTime())
                .endTime(localDateTimeEnd.toLocalTime())
                .boardroomId(reservation.getBoardroom().getId())
                .userId(reservation.getUser().getId())
                .tag(reservation.getTag())
                .build();
    }

    public List<ReservationResponseDto> toReservationResponseDtoList(List<Reservation> reservations){
        return reservations.stream().map(this::toReservationResponseDto).toList();
    }

    public Reservation toReservation(ReservationDto reservationDto) {
        LocalDateTime startLocalDateTime = dateTimeUtil.obtainLocalDateTimeFromISOString(reservationDto.startDateTime());
        LocalDateTime endLocalDateTime = dateTimeUtil.obtainLocalDateTimeFromISOString(reservationDto.endDateTime());
        return Reservation.builder()
                .meetingTitle(reservationDto.meetingTitle())
                .meetingType(reservationDto.meetingType())
                .meetingDescription(reservationDto.meetingDescription())
                .attendees(reservationDto.attendees())
                .ictSupportRequired(reservationDto.ictSupportRequired())
                .isUrgentMeeting(reservationDto.isUrgentMeeting())
                .startLocalDateTime(startLocalDateTime)
                .endLocalDateTime(endLocalDateTime)
                .build();
    }

    private String getUserTimeZone() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        return user.getUserTimeZone();
    }

}
