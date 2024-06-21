package dev.johnmaluki.boardroom_booking_backend.reservations.models;

import dev.johnmaluki.boardroom_booking_backend.boardrooms.models.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.core.models.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.users.models.AppUser;
import dev.johnmaluki.boardroom_booking_backend.utils.ApprovalStatus;
import dev.johnmaluki.boardroom_booking_backend.utils.MeetingType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="reservations")
public class Reservation extends BaseEntity {
    @Column(name="start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus = ApprovalStatus.PEDDING;

    @Column(name = "meeting_title", nullable = false)
    private String meetingTitle;

    @Column(name = "meeting_description", nullable = false, length = 1000)
    private String meetingDescription;

    @Column(name = "booked_by", nullable = false)
    private Long bookedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", nullable = false)
    private MeetingType meetingType;

    @Column(name = "attendees", nullable = false) // store coma separated string
    private String attendees;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardroom_id")
    private Boardroom boardroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;
}
