package dev.johnmaluki.boardroom_booking_backend.messaging.email;

import dev.johnmaluki.boardroom_booking_backend.messaging.event.ReservationEmailEvent;
import dev.johnmaluki.boardroom_booking_backend.messaging.rabbit.RabbitTopology;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Hands a reservation email to RabbitMQ and returns immediately.
 *
 * <p>The AMQP client is synchronous, so the publish is pushed onto {@code boundedElastic} rather
 * than run on the event loop. The returned {@code Mono} completes even if the broker is
 * unreachable: a request should not fail because a notification email could not be queued, and the
 * failure is logged for follow-up.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEmailPublisher {
  private final RabbitTemplate rabbitTemplate;

  public Mono<Void> publish(ReservationEmailEvent event) {
    if (event.to() == null || event.to().isEmpty()) {
      return Mono.empty();
    }
    return Mono.fromRunnable(
            () ->
                rabbitTemplate.convertAndSend(
                    RabbitTopology.EMAIL_EXCHANGE, RabbitTopology.EMAIL_ROUTING_KEY, event))
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(
            error ->
                log.error(
                    "Could not queue reservation email '{}': {}",
                    event.subject(),
                    error.getMessage()))
        .onErrorComplete()
        .then();
  }
}
