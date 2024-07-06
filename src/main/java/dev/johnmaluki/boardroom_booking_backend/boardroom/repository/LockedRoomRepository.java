package dev.johnmaluki.boardroom_booking_backend.boardroom.repository;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LockedRoomRepository extends JpaRepository<LockedRoom, Long> {
    List<LockedRoom> findAllByBoardroomAndLockedTrueOrderByCreatedAtDesc(Boardroom boardroom);
    @Query("SELECT l FROM LockedRoom l WHERE l.boardroom.id=:boardroomId AND l.locked=True ORDER BY createdAt DESC")
    List<LockedRoom> getLockedRoomByBoardroomId(@Param("boardroomId") Long boardroomId);
}
