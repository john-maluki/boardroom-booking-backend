package dev.johnmaluki.boardroom_booking_backend.reservation.repository;

import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStartDateAndStartTimeGreaterThan(LocalDate today, LocalTime now);

    @Query("SELECT e FROM Reservation e WHERE " +
            "(:currentDate > e.startDate OR (:currentDate = e.startDate AND :currentTime >= e.startTime)) AND " +
            "(:currentDate < e.endDate OR (:currentDate = e.endDate AND :currentTime <= e.endTime))")
    List<Reservation> findLiveMeetings(@Param("currentDate") LocalDate currentDate, @Param("currentTime") LocalTime currentTime);
    List<Reservation> findByArchivedTrueAndDeletedFalse();
}
