package dev.johnmaluki.boardroom_booking_backend.reservation.service;

import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;

import java.util.List;

public interface ReservationService {
    List<ReservationResponseDto> getAllReservations();
    /** Retrieves a list of meeting reservations that are meant to take
     * place in the future
     *
     * @return List<ReservationResponseDto>
     */
    List<ReservationResponseDto> getUpcomingReservations();
    ReservationResponseDto getReservationById(long boardroomId);
}
