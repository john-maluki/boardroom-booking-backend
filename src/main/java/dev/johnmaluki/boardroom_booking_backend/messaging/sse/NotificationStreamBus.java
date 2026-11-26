package dev.johnmaluki.boardroom_booking_backend.messaging.sse;

import tools.jackson.databind.ObjectMapper;
import dev.johnmaluki.boardroom_booking_backend.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * The fan-out bus behind the Server-Sent Events endpoint.
 *
 * <p>An SSE stream is held open by whichever instance the browser happened to connect to, but the
 * notification that should reach it may be produced anywhere — by the Rabbit worker on another
 * replica, or by a request served by a third. Redis pub/sub closes that gap: producers publish to
 * one channel and every instance relays what it receives to its own connected clients. Without it
 * an in-process {@code Sinks.Many} would work on a single node and silently drop messages the
 * moment the service is scaled past one replica.
 *
 * <p>{@link #stream()} is shared, so all SSE clients on an instance ride a single Redis
 * subscription; it is torn down when the last client disconnects and re-established on the next
 * one.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationStreamBus {
  private static final String CHANNEL = "boardroom:notifications";

  private final ReactiveStringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  /** Lazily created on first subscriber and shared by all of them. */
  private volatile Flux<NotificationResponseDto> shared;

  public Mono<Void> publish(NotificationResponseDto notification) {
    return Mono.fromCallable(() -> objectMapper.writeValueAsString(notification))
        .flatMap(payload -> redisTemplate.convertAndSend(CHANNEL, payload))
        .doOnError(
            error -> log.error("Could not publish notification to SSE bus", error))
        .onErrorComplete()
        .then();
  }

  public Flux<NotificationResponseDto> stream() {
    Flux<NotificationResponseDto> current = shared;
    if (current == null) {
      synchronized (this) {
        current = shared;
        if (current == null) {
          // defer(): listenToChannel creates its Redis listener container when it is called
          // and destroys it when the subscription ends. Without defer, share() would re-subscribe
          // to that destroyed container once the last client disconnected, and every later
          // stream would fail with "Connection is closed". retryWhen() reconnects if Redis drops.
          current =
              Flux.defer(() -> redisTemplate.listenToChannel(CHANNEL))
                  .retryWhen(
                      Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                          .maxBackoff(Duration.ofSeconds(30))
                          .doBeforeRetry(
                              signal ->
                                  log.warn(
                                      "SSE bus lost its Redis subscription, reconnecting: {}",
                                      signal.failure().getMessage())))
                  .map(message -> message.getMessage())
                  .flatMap(this::deserialise)
                  .doOnSubscribe(subscription -> log.debug("Subscribed to SSE bus {}", CHANNEL))
                  .doOnError(error -> log.error("SSE bus subscription failed", error))
                  .share();
          shared = current;
        }
      }
    }
    return current;
  }

  private Mono<NotificationResponseDto> deserialise(String payload) {
    try {
      return Mono.just(objectMapper.readValue(payload, NotificationResponseDto.class));
    } catch (Exception e) {
      log.warn("Dropping unreadable notification from SSE bus: {}", e.getMessage());
      return Mono.empty();
    }
  }
}
