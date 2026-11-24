package dev.johnmaluki.boardroom_booking_backend.messaging.event;

import java.util.Set;
import lombok.Builder;

/**
 * A reservation email waiting to be sent, carried over RabbitMQ.
 *
 * <p>The dates are already strings. The template only ever prints them, and pre-formatting here
 * means the recipient's view of the time is decided at publish time — while the acting user's
 * timezone is still known — rather than in a worker that has no request context. It also keeps the
 * payload free of temporal types whose JSON round-trip would otherwise need special handling.
 *
 * @param template Thymeleaf template to render
 * @param to recipients; one message is sent per address
 * @param detailChange drives the "details have changed" wording in the template
 */
@Builder
public record ReservationEmailEvent(
    String template,
    Set<String> to,
    String subject,
    String boardroomName,
    String bookedBy,
    String meetingType,
    String meetingTitle,
    String meetingDescription,
    String startDate,
    String endDate,
    String startTime,
    String endTime,
    String meetingLink,
    Set<String> attendees,
    boolean detailChange) {}
