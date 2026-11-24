package dev.johnmaluki.boardroom_booking_backend.notification.service.impl;

import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.service.CurrentUserService;
import dev.johnmaluki.boardroom_booking_backend.messaging.sse.NotificationStreamBus;
import dev.johnmaluki.boardroom_booking_backend.notification.dto.NotificationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.notification.mapper.NotificationMapper;
import dev.johnmaluki.boardroom_booking_backend.notification.repository.ApplicationNotificationRepository;
import dev.johnmaluki.boardroom_booking_backend.notification.repository.NotificationQueryRepository;
import dev.johnmaluki.boardroom_booking_backend.notification.service.NotificationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * In-app notifications.
 *
 * <p>Who may see what is now decided in SQL rather than by loading every recent notification and
 * filtering the list in Java — see {@link NotificationQueryRepository}.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
  private static final int FEED_WINDOW_DAYS = 10;

  private final ApplicationNotificationRepository notificationRepository;
  private final NotificationQueryRepository notificationQueryRepository;
  private final NotificationMapper notificationMapper;
  private final CurrentUserService currentUserService;
  private final NotificationStreamBus notificationStreamBus;

  @Override
  public Flux<NotificationResponseDto> getAppNotifications() {
    LocalDateTime since = LocalDateTime.now().minusDays(FEED_WINDOW_DAYS);
    return currentUserService
        .getReadFilterUserId()
        .flatMapMany(
            filterUserId ->
                notificationQueryRepository
                    .findRecentActive(since, filterUserId.orElse(null))
                    .map(notificationMapper::toNotificationResponseDto));
  }

  @Override
  @Transactional
  public Mono<NotificationResponseDto> markNotificationAsRead(Long notificationId) {
    return notificationRepository
        .findByIdAndArchivedFalseAndDeletedFalse(notificationId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Notification record not found")))
        .flatMap(
            notification -> {
              notification.setRead(true);
              return notificationRepository.save(notification);
            })
        .flatMap(saved -> notificationQueryRepository.findViewById(saved.getId()))
        .map(notificationMapper::toNotificationResponseDto)
        // Re-broadcast so other open sessions see the read state change without polling.
        .flatMap(dto -> notificationStreamBus.publish(dto).thenReturn(dto));
  }

  /**
   * Every instance relays the whole Redis channel, so each connection filters down to what its own
   * user is allowed to see. Administrators receive everything, matching the REST feed.
   */
  @Override
  public Flux<NotificationResponseDto> streamNotificationsForCurrentUser() {
    return currentUserService
        .getCurrentUser()
        .flatMapMany(
            principal ->
                notificationStreamBus
                    .stream()
                    .filter(notification -> isVisibleTo(notification, principal)));
  }

  private boolean isVisibleTo(NotificationResponseDto notification, UserPrincipal principal) {
    if (principal.isAdmin()) {
      return true;
    }
    return notification.recipient() != null
        && notification.recipient().contains(principal.getUserId());
  }
}
