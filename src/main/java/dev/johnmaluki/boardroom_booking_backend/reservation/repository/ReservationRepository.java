package dev.johnmaluki.boardroom_booking_backend.reservation.repository;

import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStartLocalDateTimeAfter(LocalDateTime currentDateTime);

    @Query("SELECT e FROM Reservation e WHERE " +
            ":currentDateTime > e.startLocalDateTime AND " +
            ":currentDateTime < e.endLocalDateTime")
    List<Reservation> findLiveMeetings(@Param("currentDateTime") LocalDateTime currentDateTime);
    List<Reservation> findByArchivedTrueAndDeletedFalse();
    Optional<Reservation> findByIdAndArchivedFalseAndDeletedFalse(long id);

    @Query("SELECT e.boardroom.id, COUNT(e.id) > 0 " +
            "FROM Reservation e " +
            "WHERE :currentDateTime BETWEEN e.startLocalDateTime AND e.endLocalDateTime " +
            "AND e.archived = false " +
            "AND e.deleted = false " +
            "GROUP BY e.boardroom.id")
    List<Object[]> findBoardroomOngoingMeetingStatus(@Param("currentDateTime") LocalDateTime currentDateTime);
}
