package dev.johnmaluki.boardroom_booking_backend.messaging.notification;

import dev.johnmaluki.boardroom_booking_backend.messaging.event.NotificationEvent;
import dev.johnmaluki.boardroom_booking_backend.messaging.rabbit.RabbitTopology;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Publishes in-app notification events so the request path does not have to write the notification
 * row, resolve its recipients and fan it out to connected clients before responding.
 *
 * <p>As with email, the synchronous AMQP publish is moved to {@code boundedElastic} and a broker
 * failure is logged rather than propagated: losing a notification must not fail the booking that
 * triggered it.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {
  private final RabbitTemplate rabbitTemplate;

  public Mono<Void> publish(NotificationEvent event) {
    return Mono.fromRunnable(
            () ->
                rabbitTemplate.convertAndSend(
                    RabbitTopology.NOTIFICATION_EXCHANGE,
                    RabbitTopology.NOTIFICATION_ROUTING_KEY,
                    event))
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(
            error ->
                log.error(
                    "Could not queue notification '{}': {}", event.title(), error.getMessage()))
        .onErrorComplete()
        .then();
  }
}
