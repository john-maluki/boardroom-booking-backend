package dev.johnmaluki.boardroom_booking_backend.boardroom.dto;

import lombok.Builder;

@Builder
public record LockedBoardroomResponseDto(long boardroomId, String givenReason){}