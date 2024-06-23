package dev.johnmaluki.boardroom_booking_backend.user.repository;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
}
