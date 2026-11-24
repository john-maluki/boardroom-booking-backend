package dev.johnmaluki.boardroom_booking_backend.notification.controller;

import dev.johnmaluki.boardroom_booking_backend.notification.dto.NotificationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Notification")
public class NotificationController {
  private static final Duration KEEP_ALIVE_INTERVAL = Duration.ofSeconds(15);

  private final NotificationService notificationService;

  @GetMapping("/notifications")
  @Operation(summary = "Fetch all user notifications not older than 10 days")
  public Flux<NotificationResponseDto> fetchUserNotifications() {
    return notificationService.getAppNotifications();
  }

  /**
   * Live notification feed, replacing the STOMP-over-WebSocket broker.
   *
   * <p>Server-Sent Events are a better fit for this: the traffic only ever goes server to client,
   * it rides ordinary HTTP so proxies and the existing auth both work unchanged, and the browser
   * reconnects on its own. The browser's {@code EventSource} cannot send an {@code Authorization}
   * header, so this endpoint also accepts the {@code access_token} cookie set at login.
   *
   * <p>A comment-only event every fifteen seconds keeps intermediaries from closing an idle
   * connection; clients ignore it, since a comment carries no data.
   */
  @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "Subscribe to live notifications over Server-Sent Events")
  public Flux<ServerSentEvent<NotificationResponseDto>> streamNotifications() {
    Flux<ServerSentEvent<NotificationResponseDto>> notifications =
        notificationService
            .streamNotificationsForCurrentUser()
            .map(
                notification ->
                    ServerSentEvent.builder(notification)
                        .id(String.valueOf(notification.id()))
                        .event("notification")
                        .build());

    Flux<ServerSentEvent<NotificationResponseDto>> keepAlive =
        Flux.interval(KEEP_ALIVE_INTERVAL)
            .map(
                tick ->
                    ServerSentEvent.<NotificationResponseDto>builder()
                        .comment("keep-alive")
                        .build());

    return Flux.merge(notifications, keepAlive);
  }

  @PatchMapping("/notifications/{notificationId}")
  @Operation(summary = "Mark notification as read")
  public Mono<NotificationResponseDto> markNotificationAsRead(
      @PathVariable("notificationId") Long notificationId) {
    return notificationService.markNotificationAsRead(notificationId);
  }
}
