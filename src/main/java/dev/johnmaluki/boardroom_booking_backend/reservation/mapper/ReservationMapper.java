package dev.johnmaluki.boardroom_booking_backend.reservation.mapper;


import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationMapper {
    public ReservationResponseDto toReservationResponseDto(Reservation reservation) {
        return ReservationResponseDto.builder()
                .id(reservation.getId())
                .meetingTitle(reservation.getMeetingTitle())
                .meetingDescription(reservation.getMeetingDescription())
                .meetingType(reservation.getMeetingType())
                .approvalStatus(reservation.getApprovalStatus())
                .attendees(reservation.getAttendees())
                .startDate(reservation.getStartDate())
                .endDate(reservation.getEndDate())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getStartTime())
                .boardroom(reservation.getBoardroom().getId())
                .tag(reservation.getTag())
                .build();
    }

    public List<ReservationResponseDto> toReservationResponseDtoList(List<Reservation> reservations){
        return reservations.stream().map(this::toReservationResponseDto).toList();
    }
}
