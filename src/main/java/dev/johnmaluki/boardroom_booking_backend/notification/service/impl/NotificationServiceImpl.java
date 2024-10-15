package dev.johnmaluki.boardroom_booking_backend.notification.service.impl;

import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.service.CurrentUserService;
import dev.johnmaluki.boardroom_booking_backend.notification.dto.NotificationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.notification.mapper.NotificationMapper;
import dev.johnmaluki.boardroom_booking_backend.notification.model.ApplicationNotification;
import dev.johnmaluki.boardroom_booking_backend.notification.repository.ApplicationNotificationRepository;
import dev.johnmaluki.boardroom_booking_backend.notification.service.NotificationService;
import dev.johnmaluki.boardroom_booking_backend.notification.util.NotificationUtil;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
  private final Environment env;
  private final ApplicationNotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final CurrentUserService currentUserService;
  private final UserUtil userUtil;
  private final NotificationUtil notificationUtil;

  @Override
  public List<NotificationResponseDto> getAppNotifications() {
    LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
    List<ApplicationNotification> applicationNotifications =
        this.filterNotificationsBasedOnCurrentUser(
            notificationRepository.findRecentActiveNotifications(tenDaysAgo));

    return notificationMapper.toNotificationResponseDtoList(applicationNotifications);
  }

  @Override
  public NotificationResponseDto markNotificationAsRead(Long notificationId) {
    ApplicationNotification applicationNotification = this.getNotificationById(notificationId);
    applicationNotification.setRead(true);
    NotificationResponseDto notificationResponseDto =
        notificationMapper.toNotificationResponseDto(
            notificationRepository.save(applicationNotification));
    this.sendNotificationUpdate(notificationResponseDto);
    return notificationResponseDto;
  }

  private void sendNotificationUpdate(NotificationResponseDto notificationResponseDto) {
    notificationUtil.sendNotificationToAdmins(notificationResponseDto);
  }

  private List<ApplicationNotification> filterNotificationsBasedOnCurrentUser(
      List<ApplicationNotification> notifications) {
    AppUser authUser = currentUserService.getAppUser();
    Role role = authUser.getRole();

    return role.getAuthority() == RoleType.ADMIN
        ? notifications
        : notifications.stream()
            .filter(
                notification -> {
                  List<Long> recipients =
                      userUtil.getRecipientsFromString(notification.getRecipient());
                  return recipients.contains(authUser.getId());
                })
            .toList();
  }

  private ApplicationNotification getNotificationById(Long notificationId) {
    return notificationRepository
        .findById(notificationId)
        .orElseThrow(() -> new ResourceNotFoundException("Notification record not found"));
  }
}
