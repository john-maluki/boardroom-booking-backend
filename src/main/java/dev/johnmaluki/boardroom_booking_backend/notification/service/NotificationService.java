package dev.johnmaluki.boardroom_booking_backend.notification.service;

import dev.johnmaluki.boardroom_booking_backend.notification.dto.NotificationResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {
  Flux<NotificationResponseDto> getAppNotifications();

  Mono<NotificationResponseDto> markNotificationAsRead(Long notificationId);

  /**
   * The live notification feed for the acting user, for the Server-Sent Events endpoint to relay.
   *
   * @return notifications addressed to this user as they are raised
   */
  Flux<NotificationResponseDto> streamNotificationsForCurrentUser();
}
