package dev.johnmaluki.boardroom_booking_backend.notification.controller;

import dev.johnmaluki.boardroom_booking_backend.notification.dto.NotificationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Notification")
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping("/notifications")
  @Operation(summary = "Fetch all user notifications not older than 3 days")
  public ResponseEntity<List<NotificationResponseDto>> fetchUserNotifications() {
    return ResponseEntity.ok(notificationService.getAppNotifications());
  }

  @PatchMapping("/notifications/{notificationId}")
  @Operation(summary = "Mark notification as read")
  public ResponseEntity<NotificationResponseDto> markNotificationAsRead(
      @PathVariable("notificationId") Long notificationId) {
    return ResponseEntity.ok(notificationService.markNotificationAsRead(notificationId));
  }
}
