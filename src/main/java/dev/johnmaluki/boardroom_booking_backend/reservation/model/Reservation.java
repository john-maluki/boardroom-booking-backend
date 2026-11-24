package dev.johnmaluki.boardroom_booking_backend.reservation.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import dev.johnmaluki.boardroom_booking_backend.util.MeetingType;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table("reservations")
public class Reservation extends BaseEntity {
  @Column("start_date_time")
  private LocalDateTime startLocalDateTime;

  @Column("end_date_time")
  private LocalDateTime endLocalDateTime;

  @Builder.Default
  @Column("approval_status")
  private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

  @Column("cancellation_message")
  private String cancellationMessage;

  @Column("meeting_title")
  private String meetingTitle;

  @Column("meeting_description")
  private String meetingDescription;

  @Column("meeting_type")
  private MeetingType meetingType;

  /** Comma separated attendee emails. */
  @Column("attendees")
  private String attendees;

  @Builder.Default
  @Column("ict_support_required")
  private boolean ictSupportRequired = true;

  @Builder.Default
  @Column("is_urgent_meeting")
  private boolean isUrgentMeeting = false;

  @Builder.Default
  @Column("record_meeting")
  private boolean recordMeeting = false;

  @Column("meeting_link")
  private String meetingLink;

  @Column("boardroom_id")
  private Long boardroomId;

  @Column("user_id")
  private Long userId;
}
