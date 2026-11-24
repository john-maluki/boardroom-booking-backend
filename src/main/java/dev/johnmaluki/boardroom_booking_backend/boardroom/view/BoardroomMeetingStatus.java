package dev.johnmaluki.boardroom_booking_backend.boardroom.view;

/** Whether a boardroom currently has a meeting in progress. */
public record BoardroomMeetingStatus(Long boardroomId, boolean hasOngoingMeeting) {}
