package dev.johnmaluki.boardroom_booking_backend.notification.service;

import dev.johnmaluki.boardroom_booking_backend.notification.dto.NotificationResponseDto;
import java.util.List;

public interface NotificationService {
  List<NotificationResponseDto> getAppNotifications();

  NotificationResponseDto markNotificationAsRead(Long notificationId);
}
