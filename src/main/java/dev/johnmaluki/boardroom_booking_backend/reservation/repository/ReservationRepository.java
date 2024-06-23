package dev.johnmaluki.boardroom_booking_backend.reservation.repository;

import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
