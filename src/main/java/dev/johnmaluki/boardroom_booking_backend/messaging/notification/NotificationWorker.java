package dev.johnmaluki.boardroom_booking_backend.messaging.notification;

import dev.johnmaluki.boardroom_booking_backend.messaging.event.NotificationEvent;
import dev.johnmaluki.boardroom_booking_backend.messaging.rabbit.RabbitTopology;
import dev.johnmaluki.boardroom_booking_backend.messaging.sse.NotificationStreamBus;
import dev.johnmaluki.boardroom_booking_backend.notification.mapper.NotificationMapper;
import dev.johnmaluki.boardroom_booking_backend.notification.model.ApplicationNotification;
import dev.johnmaluki.boardroom_booking_backend.notification.repository.ApplicationNotificationRepository;
import dev.johnmaluki.boardroom_booking_backend.notification.repository.NotificationQueryRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Turns a queued {@link NotificationEvent} into a persisted notification and pushes it to every
 * connected SSE client.
 *
 * <p>The listener container is thread-based, so the reactive pipeline is driven to completion with
 * {@code block()} here. That is deliberate and safe: this runs on a Rabbit listener thread, never
 * on an event loop, and blocking is what gives the broker a definite success or failure to apply
 * its acknowledgement and retry policy to.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWorker {
  private static final Duration PROCESSING_TIMEOUT = Duration.ofSeconds(30);

  private final ApplicationNotificationRepository notificationRepository;
  private final NotificationQueryRepository notificationQueryRepository;
  private final AppUserRepository userRepository;
  private final NotificationMapper notificationMapper;
  private final NotificationStreamBus notificationStreamBus;
  private final UserUtil userUtil;

  @RabbitListener(queues = RabbitTopology.NOTIFICATION_QUEUE)
  public void onNotification(NotificationEvent event) {
    resolveRecipients(event)
        .flatMap(recipients -> persistAndBroadcast(event, recipients))
        .block(PROCESSING_TIMEOUT);
  }

  /**
   * The event names the direct recipients; administrators are resolved here rather than at publish
   * time so the list reflects who is an administrator when the notification is actually raised.
   */
  private Mono<List<Long>> resolveRecipients(NotificationEvent event) {
    List<Long> direct = event.recipients() == null ? List.of() : event.recipients();
    if (!event.alsoNotifyAdmins()) {
      return Mono.just(direct);
    }
    return userRepository
        .findApplicationAdministrators()
        .map(dev.johnmaluki.boardroom_booking_backend.user.model.AppUser::getId)
        .collectList()
        .map(
            admins -> {
              Set<Long> merged = new LinkedHashSet<>(direct);
              merged.addAll(admins);
              return new ArrayList<>(merged);
            });
  }

  private Mono<Void> persistAndBroadcast(NotificationEvent event, List<Long> recipients) {
    if (recipients.isEmpty()) {
      log.warn("Dropping notification '{}' because it has no recipients", event.title());
      return Mono.empty();
    }
    ApplicationNotification notification =
        ApplicationNotification.builder()
            .title(event.title())
            .message(event.message())
            .reservationId(event.reservationId())
            .recipient(userUtil.recipientsToString(recipients))
            .build();
    return notificationRepository
        .save(notification)
        .flatMap(saved -> notificationQueryRepository.findViewById(saved.getId()))
        .map(notificationMapper::toNotificationResponseDto)
        .flatMap(notificationStreamBus::publish)
        .then();
  }

}
