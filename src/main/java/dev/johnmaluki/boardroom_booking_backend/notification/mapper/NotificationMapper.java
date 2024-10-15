package dev.johnmaluki.boardroom_booking_backend.notification.mapper;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.notification.dto.NotificationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.notification.model.ApplicationNotification;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {
  private final UserUtil userUtil;

  public NotificationResponseDto toNotificationResponseDto(
      ApplicationNotification applicationNotification) {
    Reservation reservation = applicationNotification.getReservation();
    Boardroom boardroom = reservation.getBoardroom();
    AppUser user = applicationNotification.getReservation().getUser();
    List<Long> recipients =
        userUtil.getRecipientsFromString(applicationNotification.getRecipient());
    return NotificationResponseDto.builder()
        .id(applicationNotification.getId())
        .title(applicationNotification.getTitle())
        .message(applicationNotification.getMessage())
        .isRead(applicationNotification.isRead())
        .boardroomName(boardroom.getName())
        .createdAt(applicationNotification.getCreatedAt())
        .createdBy(user.getFullName())
        .recipient(recipients)
        .createdByEmail(user.getEmail())
        .build();
  }

  public List<NotificationResponseDto> toNotificationResponseDtoList(
      List<ApplicationNotification> applicationNotifications) {
    return applicationNotifications.stream().map(this::toNotificationResponseDto).toList();
  }
}
