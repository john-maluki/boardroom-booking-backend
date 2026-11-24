package dev.johnmaluki.boardroom_booking_backend.notification.mapper;

import dev.johnmaluki.boardroom_booking_backend.notification.dto.NotificationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.notification.view.NotificationView;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {
  private final UserUtil userUtil;

  public NotificationResponseDto toNotificationResponseDto(NotificationView view) {
    return NotificationResponseDto.builder()
        .id(view.id())
        .title(view.title())
        .message(view.message())
        .isRead(view.read())
        .boardroomName(view.boardroomName())
        .createdAt(view.createdAt())
        .createdBy(view.createdByFullName())
        .recipient(userUtil.getRecipientsFromString(view.recipient()))
        .createdByEmail(view.createdByEmail())
        .build();
  }
}
