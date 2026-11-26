package dev.johnmaluki.boardroom_booking_backend.notification.repository;

import dev.johnmaluki.boardroom_booking_backend.notification.model.ApplicationNotification;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ApplicationNotificationRepository
    extends ReactiveCrudRepository<ApplicationNotification, Long> {
  Mono<ApplicationNotification> findByIdAndArchivedFalseAndDeletedFalse(long notificationId);

  /**
   * Removes every notification raised for a reservation. Needed before a reservation row can be
   * hard-deleted, since notifications.reservation_id references it.
   */
  @Modifying
  @Query("DELETE FROM notifications WHERE reservation_id = :reservationId")
  Mono<Integer> deleteByReservationId(long reservationId);
}
