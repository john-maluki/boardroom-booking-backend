package dev.johnmaluki.boardroom_booking_backend.boardroom.repository;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardroomRepository extends JpaRepository<Boardroom, Long> {
    List<Boardroom> findByArchivedFalseAndDeletedFalse();
    Optional<Boardroom> findByIdAndArchivedFalseAndDeletedFalse(long boardroomId);
    List<Boardroom> findAllByLockedTrueAndDeletedFalseAndArchivedFalse();
}
