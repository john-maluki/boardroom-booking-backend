package dev.johnmaluki.boardroom_booking_backend.reservation.service;

import dev.johnmaluki.boardroom_booking_backend.messaging.email.ReservationEmailPublisher;
import dev.johnmaluki.boardroom_booking_backend.messaging.event.NotificationEvent;
import dev.johnmaluki.boardroom_booking_backend.messaging.event.ReservationEmailEvent;
import dev.johnmaluki.boardroom_booking_backend.messaging.notification.NotificationEventPublisher;
import dev.johnmaluki.boardroom_booking_backend.reservation.view.ReservationView;
import dev.johnmaluki.boardroom_booking_backend.user.service.UserServiceUtil;
import dev.johnmaluki.boardroom_booking_backend.util.DateTimeUtil;
import dev.johnmaluki.boardroom_booking_backend.util.EmailUtil;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Everything a reservation change has to tell people about.
 *
 * <p>Pulled out of the service so that the write paths read as the state change they actually are,
 * and so the message construction — which is the bulk of the old service — lives in one place.
 *
 * <p>Each method publishes to RabbitMQ and returns a {@code Mono} that completes as soon as the
 * message is queued. The recipient's view of the meeting times is formatted here, while the acting
 * user's timezone is still known, because the Rabbit worker runs with no request context.
 */
@Service
@RequiredArgsConstructor
public class ReservationNotifier {
  private static final String TEMPLATE_APPROVAL_REQUEST = "emailReservationTemplate.html";
  private static final String TEMPLATE_RESERVATION = "reservationTemplate.html";

  private final ReservationEmailPublisher emailPublisher;
  private final NotificationEventPublisher notificationPublisher;
  private final UserServiceUtil userServiceUtil;
  private final DateTimeUtil dateTimeUtil;

  /** Asks the boardroom's administrator to approve a new booking. */
  public Mono<Void> requestApproval(ReservationView view, String timeZone) {
    if (view.boardroomAdministratorId() == null) {
      return Mono.empty();
    }
    return notificationPublisher
        .publish(
            new NotificationEvent(
                view.id(),
                "Reservation pending approval",
                view.meetingTitle(),
                List.of(view.boardroomAdministratorId()),
                // Administrators are copied in so a pending booking is visible to them too.
                true))
        .then(
            administratorEmail(view)
                .flatMap(
                    to ->
                        emailPublisher.publish(
                            baseEmail(view, timeZone, TEMPLATE_APPROVAL_REQUEST)
                                .to(Set.of(to))
                                .subject(EmailUtil.SUBJECT_RESERVATION_CONFIRMATION)
                                .build()))
                .then());
  }

  /** Tells the booker and the administrators that a booking was approved. */
  public Mono<Void> announceApproval(ReservationView view) {
    return notificationPublisher.publish(
        new NotificationEvent(
            view.id(),
            "Reservation  Approved",
            view.meetingTitle(),
            List.of(view.userId()),
            true));
  }

  /** Asks the system administrators to create a meeting link for an approved online meeting. */
  public Mono<Void> requestMeetingLink(ReservationView view, String timeZone) {
    return systemAdministratorEmails()
        .flatMap(
            emails ->
                emailPublisher.publish(
                    baseEmail(view, timeZone, TEMPLATE_RESERVATION)
                        .to(emails)
                        .subject(EmailUtil.SUBJECT_RESERVATION_ADMIN_APPROVAL)
                        .build()));
  }

  /** Sends the finished meeting details, including the link, to attendees and the booker. */
  public Mono<Void> announceMeetingLink(ReservationView view, String timeZone) {
    return emailPublisher.publish(
        baseEmail(view, timeZone, TEMPLATE_RESERVATION)
            .to(attendeesPlusCreator(view))
            .subject(EmailUtil.SUBJECT_RESERVATION_MEETING_LINK)
            .build());
  }

  /** Tells attendees and administrators that something about the meeting changed. */
  public Mono<Void> announceDetailChange(ReservationView view, String timeZone, String subject) {
    ReservationEmailEvent.ReservationEmailEventBuilder base =
        baseEmail(view, timeZone, TEMPLATE_RESERVATION).subject(subject).detailChange(true);
    return emailPublisher
        .publish(base.to(attendeesPlusCreator(view)).build())
        .then(
            systemAdministratorEmails()
                .flatMap(emails -> emailPublisher.publish(base.to(emails).build()))
                .then());
  }

  private Mono<String> administratorEmail(ReservationView view) {
    if (view.boardroomAdministratorId() == null) {
      return Mono.empty();
    }
    return userServiceUtil
        .findEmailByUserId(view.boardroomAdministratorId())
        .filter(email -> !email.isBlank());
  }

  private Mono<Set<String>> systemAdministratorEmails() {
    return userServiceUtil
        .getAllSystemAdministrators()
        .map(AppUser::getEmail)
        .filter(email -> email != null && !email.isBlank())
        .collectList()
        .map(LinkedHashSet::new);
  }

  private ReservationEmailEvent.ReservationEmailEventBuilder baseEmail(
      ReservationView view, String timeZone, String template) {
    LocalDateTime start =
        dateTimeUtil.obtainLocalDateTimeBasedOnUserZone(view.startLocalDateTime(), timeZone);
    LocalDateTime end =
        dateTimeUtil.obtainLocalDateTimeBasedOnUserZone(view.endLocalDateTime(), timeZone);
    return ReservationEmailEvent.builder()
        .template(template)
        .boardroomName(view.boardroomName())
        .bookedBy(view.userEmail())
        .meetingType(view.meetingType() == null ? null : view.meetingType().name())
        .meetingTitle(view.meetingTitle())
        .meetingDescription(view.meetingDescription())
        .startDate(String.valueOf(start.toLocalDate()))
        .endDate(String.valueOf(end.toLocalDate()))
        .startTime(String.valueOf(start.toLocalTime()))
        .endTime(String.valueOf(end.toLocalTime()))
        .meetingLink(view.meetingLink())
        .attendees(attendeesPlusCreator(view))
        .detailChange(false);
  }

  /** The attendee list from the stored CSV, plus the booker and the boardroom's own address. */
  private Set<String> attendeesPlusCreator(ReservationView view) {
    Set<String> recipients = new LinkedHashSet<>();
    if (view.attendees() != null) {
      Arrays.stream(view.attendees().split(","))
          .map(String::trim)
          .filter(attendee -> !attendee.isEmpty())
          .forEach(recipients::add);
    }
    if (view.boardroomEmail() != null && !view.boardroomEmail().isEmpty()) {
      recipients.add(view.boardroomEmail());
    }
    if (view.userEmail() != null) {
      recipients.add(view.userEmail());
    }
    return recipients;
  }
}
