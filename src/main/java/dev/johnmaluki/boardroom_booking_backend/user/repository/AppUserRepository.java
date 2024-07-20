package dev.johnmaluki.boardroom_booking_backend.user.repository;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByIdAndArchivedFalseAndDeletedFalse(long userId);

    @Query(
            value = "SELECT u FROM AppUser u JOIN u.role r WHERE r.authority = 'ADMIN' " +
                    "AND u.deleted = false AND u.archived = false"
    )
    List<AppUser> findApplicationAdministrators();
}
