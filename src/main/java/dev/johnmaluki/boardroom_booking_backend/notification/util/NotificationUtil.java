package dev.johnmaluki.boardroom_booking_backend.notification.util;

import dev.johnmaluki.boardroom_booking_backend.notification.dto.NotificationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationUtil {
  private final SimpMessagingTemplate simpMessagingTemplate;

  public void sendNotificationToUser(
      AppUser user, NotificationResponseDto notificationResponseDto) {
    simpMessagingTemplate.convertAndSendToUser(user.getTag(), "/private", notificationResponseDto);
  }

  public void sendNotificationToAdmins(NotificationResponseDto notificationResponseDto) {
    simpMessagingTemplate.convertAndSend("/admin/room", notificationResponseDto);
  }
}
