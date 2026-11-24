package dev.johnmaluki.boardroom_booking_backend.messaging.event;

import java.util.List;

/**
 * A request to raise an in-app notification, carried over RabbitMQ.
 *
 * <p>It intentionally holds ids and text rather than entities: the worker reloads what it needs,
 * which keeps the message small and means a message sitting in the queue never carries a stale copy
 * of a row.
 *
 * @param reservationId the reservation the notification hangs off
 * @param title headline shown in the notification list
 * @param message body, currently the meeting title
 * @param recipients user ids the notification is addressed to
 * @param alsoNotifyAdmins whether every system administrator should additionally receive it
 */
public record NotificationEvent(
    Long reservationId,
    String title,
    String message,
    List<Long> recipients,
    boolean alsoNotifyAdmins) {}
