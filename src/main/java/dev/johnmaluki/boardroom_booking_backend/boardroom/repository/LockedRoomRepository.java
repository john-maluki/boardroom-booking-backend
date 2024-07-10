package dev.johnmaluki.boardroom_booking_backend.boardroom.repository;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LockedRoomRepository extends JpaRepository<LockedRoom, Long> {
    Optional<LockedRoom> getByBoardroomAndLockedTrueAndArchivedFalseAndDeletedFalse(Boardroom boardroom);
}
