package dev.johnmaluki.boardroom_booking_backend.boardroom.service;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;

import java.util.List;

public interface BoardroomService {
    List<BoardroomResponseDto> getAllBoardrooms();
    BoardroomResponseDto getBoardroomById(long tag);
}
