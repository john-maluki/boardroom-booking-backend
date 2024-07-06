package dev.johnmaluki.boardroom_booking_backend.reservation.service.impl;

import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.mapper.ReservationMapper;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    @Override
    public List<ReservationResponseDto> getAllReservations() {
        return reservationMapper.toReservationResponseDtoList(reservationRepository.findAll());
    }

    @Override
    public List<ReservationResponseDto> getUpcomingReservations() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return reservationMapper.toReservationResponseDtoList(
                reservationRepository.findByStartDateAndStartTimeGreaterThan(today, now)
        );
    }

    @Override
    public List<ReservationResponseDto> getAllLiveMeetings() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return reservationMapper.toReservationResponseDtoList(
                reservationRepository.findLiveMeetings(today, now)
        );
    }

    @Override
    public ReservationResponseDto getReservationById(long id) {
        return reservationMapper.toReservationResponseDto(
                reservationRepository.findById(id).orElseThrow(
                        () -> new ResourceNotFoundException("Resource could not be found")
                )
        );
    }
}
