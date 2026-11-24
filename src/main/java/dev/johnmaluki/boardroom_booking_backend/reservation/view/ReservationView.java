package dev.johnmaluki.boardroom_booking_backend.reservation.view;

import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import dev.johnmaluki.boardroom_booking_backend.util.MeetingType;
import java.time.LocalDateTime;

/**
 * A reservation already joined to its boardroom and its owner.
 *
 * <p>R2DBC has no lazy associations, so rather than fetch a reservation and then issue a query per
 * row for the boardroom and the user, the read paths select the joined shape once and map it
 * straight into this record. That keeps every list endpoint at a single round trip.
 */
public record ReservationView(
    Long id,
    String tag,
    LocalDateTime startLocalDateTime,
    LocalDateTime endLocalDateTime,
    ApprovalStatus approvalStatus,
    String cancellationMessage,
    String meetingTitle,
    String meetingDescription,
    MeetingType meetingType,
    String attendees,
    boolean ictSupportRequired,
    boolean urgentMeeting,
    boolean recordMeeting,
    String meetingLink,
    LocalDateTime createdAt,
    Long boardroomId,
    String boardroomName,
    String boardroomEmail,
    Long boardroomAdministratorId,
    Long userId,
    String userFullName,
    String userEmail) {}
