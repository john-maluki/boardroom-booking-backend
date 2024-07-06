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

    /** Retrieves a list of live meetings where the current date and time fall between
     * the start and end dates and times of each meeting.
     *
     * @return List of live meetings
     */
    List<ReservationResponseDto> getAllLiveMeetings();
    ReservationResponseDto getReservationById(long boardroomId);
}
