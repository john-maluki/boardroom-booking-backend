package dev.johnmaluki.boardroom_booking_backend.reservation.model;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import dev.johnmaluki.boardroom_booking_backend.util.MeetingType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="reservations")
public class Reservation extends BaseEntity {
    @Column(name="start_date_time")
    private LocalDateTime startLocalDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endLocalDateTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "meeting_title", nullable = false, length = 1000)
    private String meetingTitle;

    @Column(name = "meeting_description", nullable = false, length = 2000)
    private String meetingDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", nullable = false)
    private MeetingType meetingType;

    @Column(name = "attendees", nullable = false) // store coma separated string
    private String attendees;

    @Builder.Default
    @Column(name = "ict_support_required", nullable = false)
    private boolean ictSupportRequired = true;

    @Builder.Default
    @Column(name = "is_urgent_meeting", nullable = false)
    private boolean isUrgentMeeting = false;

    @Builder.Default
    @Column(name = "record_meeting", nullable = false)
    private boolean recordMeeting = false;

    @Column(name = "meeting_link")
    private String meetingLink;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardroom_id")
    private Boardroom boardroom;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;
}
