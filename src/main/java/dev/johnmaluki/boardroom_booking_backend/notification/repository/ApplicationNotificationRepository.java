package dev.johnmaluki.boardroom_booking_backend.notification.repository;

import dev.johnmaluki.boardroom_booking_backend.notification.model.ApplicationNotification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationNotificationRepository
    extends JpaRepository<ApplicationNotification, Long> {

  @Query(
      """
    SELECT n
    FROM ApplicationNotification n
    WHERE n.archived = false
      AND n.deleted = false
      AND n.createdAt >= :tenDaysAgo
    ORDER BY n.createdAt DESC
    """)
  List<ApplicationNotification> findRecentActiveNotifications(
      @Param("tenDaysAgo") LocalDateTime tenDaysAgo);
}
