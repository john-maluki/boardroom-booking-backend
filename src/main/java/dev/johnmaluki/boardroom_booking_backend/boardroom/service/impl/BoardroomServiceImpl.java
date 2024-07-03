package dev.johnmaluki.boardroom_booking_backend.boardroom.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.BoardroomRepository;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardroomServiceImpl implements BoardroomService {
    private final BoardroomRepository boardroomRepository;
    @Override
    public List<BoardroomResponseDto> getAllBoardrooms() {
        return boardroomRepository.findAll().stream().map(
                boardroom ->  BoardroomResponseDto.builder()
                            .email(boardroom.getEmail())
                            .name(boardroom.getName())
                            .locked(boardroom.isLocked())
                            .centre(boardroom.getCentre())
                            .department(boardroom.getDepartment())
                            .internetEnabled(boardroom.isInternetEnabled())
                            .meetingTypeSupported(boardroom.getMeetingTypeSupported())
                            .build()

        ).toList();
    }
}
