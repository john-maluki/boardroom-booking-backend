package dev.johnmaluki.boardroom_booking_backend.notification.view;

import java.time.LocalDateTime;

/**
 * A notification joined to the reservation it belongs to, plus that reservation's boardroom name
 * and the name/email of whoever booked it. Same motivation as {@link
 * dev.johnmaluki.boardroom_booking_backend.reservation.view.ReservationView}: one query instead of
 * a fan-out per row.
 */
public record NotificationView(
    Long id,
    String title,
    String message,
    String recipient,
    boolean read,
    LocalDateTime createdAt,
    Long reservationId,
    String boardroomName,
    String createdByFullName,
    String createdByEmail) {}
