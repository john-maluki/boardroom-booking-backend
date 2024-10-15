package dev.johnmaluki.boardroom_booking_backend.notification.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record NotificationResponseDto(
    Long id,
    String message,
    String title,
    LocalDateTime createdAt,
    boolean isRead,
    String createdBy,
    String createdByEmail,
    List<Long> recipient,
    String boardroomName) {}
