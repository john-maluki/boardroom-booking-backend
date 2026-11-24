package dev.johnmaluki.boardroom_booking_backend.notification.repository;

import dev.johnmaluki.boardroom_booking_backend.notification.model.ApplicationNotification;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ApplicationNotificationRepository
    extends ReactiveCrudRepository<ApplicationNotification, Long> {
  Mono<ApplicationNotification> findByIdAndArchivedFalseAndDeletedFalse(long notificationId);
}
