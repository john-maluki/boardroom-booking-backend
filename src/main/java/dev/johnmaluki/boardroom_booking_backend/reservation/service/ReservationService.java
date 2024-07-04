package dev.johnmaluki.boardroom_booking_backend.reservation.service;

import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;

import java.util.List;

public interface ReservationService {
    public List<ReservationResponseDto> getAllReservations();
}
