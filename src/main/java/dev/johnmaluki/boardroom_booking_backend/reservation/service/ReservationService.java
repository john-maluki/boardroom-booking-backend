package dev.johnmaluki.boardroom_booking_backend.reservation.service;

import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;

import java.util.List;

public interface ReservationService {
    List<ReservationResponseDto> getAllReservations();
    ReservationResponseDto getReservationById(long id);
}
