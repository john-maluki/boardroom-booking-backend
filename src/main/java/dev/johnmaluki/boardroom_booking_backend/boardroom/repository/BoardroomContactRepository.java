package dev.johnmaluki.boardroom_booking_backend.boardroom.repository;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.BoardroomContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardroomContactRepository extends JpaRepository<BoardroomContact, Long> {
    @Query(
            "SELECT c FROM BoardroomContact c WHERE c.boardroom.id = :boardroomId " +
                    "AND c.id = :contactId AND c.archived = false AND c.deleted = false"
    )
    BoardroomContact getContactRecord(
            @Param("contactId") long contactId, @Param("boardroomId") long boardroomId);
}
