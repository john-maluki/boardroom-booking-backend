package dev.johnmaluki.boardroom_booking_backend.user.repository;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppAdminRepository extends JpaRepository<AppAdmin, Long> {
    boolean existsByEmail(String email);
}
