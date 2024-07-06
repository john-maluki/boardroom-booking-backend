package dev.johnmaluki.boardroom_booking_backend.reservation.mapper;


import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
public class ReservationMapper {
    public ReservationResponseDto toReservationResponseDto(Reservation reservation) {
        String userTimeZone = this.getUserTimeZone();
        ZonedDateTime zonedDateTimeStart = this.getZonedDateTime(reservation.getStartDate(), reservation.getStartTime(), userTimeZone);
        ZonedDateTime zonedDateTimeEnd = this.getZonedDateTime(reservation.getEndDate(), reservation.getEndTime(), userTimeZone);
        return ReservationResponseDto.builder()
                .id(reservation.getId())
                .meetingTitle(reservation.getMeetingTitle())
                .meetingDescription(reservation.getMeetingDescription())
                .meetingType(reservation.getMeetingType())
                .approvalStatus(reservation.getApprovalStatus())
                .attendees(reservation.getAttendees())
                .startDate(zonedDateTimeStart.toLocalDate())
                .endDate(zonedDateTimeEnd.toLocalDate())
                .startTime(zonedDateTimeStart.toLocalTime())
                .endTime(zonedDateTimeEnd.toLocalTime())
                .boardroomId(reservation.getBoardroom().getId())
                .tag(reservation.getTag())
                .build();
    }

    public List<ReservationResponseDto> toReservationResponseDtoList(List<Reservation> reservations){
        return reservations.stream().map(this::toReservationResponseDto).toList();
    }

    private ZonedDateTime getZonedDateTime(LocalDate date, LocalTime time, String userTimeZone) {
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        ZoneId originalZoneId = ZoneId.of("UTC");
        ZoneId targetZoneId = ZoneId.of(userTimeZone);
        ZonedDateTime originalZonedDateTime = dateTime.atZone(originalZoneId);
        return originalZonedDateTime.withZoneSameInstant(targetZoneId);
    }

    private String getUserTimeZone() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        return user.getUserTimeZone();
    }
}
