package dev.johnmaluki.boardroom_booking_backend.reservation.service.impl;

import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceOwnershipException;
import dev.johnmaluki.boardroom_booking_backend.core.service.CurrentUserService;
import dev.johnmaluki.boardroom_booking_backend.core.util.DataFilterUtil;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.mapper.ReservationMapper;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationService;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final CurrentUserService currentUserService;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    @Override
    public List<ReservationResponseDto> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        reservations = this.filterUseReservations(reservations);
        return reservationMapper.toReservationResponseDtoList(
                new DataFilterUtil<Reservation>().removeArchivedAndDeletedRecords(reservations)
        );
    }

    @Override
    public List<ReservationResponseDto> getUpcomingReservations() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return reservationMapper.toReservationResponseDtoList(
                new DataFilterUtil<Reservation>().removeArchivedAndDeletedRecords(
                        this.filterUseReservations(reservationRepository.findByStartDateAndStartTimeGreaterThan(today, now))
                )
        );
    }

    @Override
    public List<ReservationResponseDto> getAllLiveMeetings() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return reservationMapper.toReservationResponseDtoList(
                new DataFilterUtil<Reservation>().removeArchivedAndDeletedRecords(
                        this.filterUseReservations(reservationRepository.findLiveMeetings(today, now))
                )
        );
    }

    @Override
    public List<ReservationResponseDto> getArchivedReservations() {
        return reservationMapper.toReservationResponseDtoList(
                this.filterUseReservations(reservationRepository.findByArchivedTrueAndDeletedFalse())
        );
    }

    @Override
    public ReservationResponseDto getReservationById(long id) {
        Reservation reservation = reservationRepository.findByIdAndArchivedFalseAndDeletedFalse(id).orElseThrow(
                () -> new ResourceNotFoundException("Resource could not be found")
        );
        if (!this.hasOwnership(reservation)) {
            throw new ResourceOwnershipException("Not allowed to access");
        }
        return reservationMapper.toReservationResponseDto(reservation);
    }

    private List<Reservation> filterUseReservations(List<Reservation> reservations) {
        if (currentUserService.getUserRole() != RoleType.ADMIN) {
            return reservations.stream().filter(
                    reservation -> reservation.getUser().getId() == currentUserService.getUserId()
            ).toList();
        } else {
            return reservations;
        }
    }

    private boolean hasOwnership(Reservation reservation) {
        if (currentUserService.getUserRole() == RoleType.ADMIN) {
            return true;
        }
        return currentUserService.getUserId() == reservation.getUser().getId();
    }

}
