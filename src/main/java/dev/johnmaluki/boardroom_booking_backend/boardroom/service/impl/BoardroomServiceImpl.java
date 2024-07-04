package dev.johnmaluki.boardroom_booking_backend.boardroom.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.mapper.BoardroomMapper;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.BoardroomRepository;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomService;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardroomServiceImpl implements BoardroomService {
    private final BoardroomRepository boardroomRepository;
    private final BoardroomMapper boardroomMapper;
    @Override
    public List<BoardroomResponseDto> getAllBoardrooms() {
        return boardroomMapper.toBoardroomResponseDtoList(boardroomRepository.findAll());
    }

    @Override
    public BoardroomResponseDto getBoardroomById(long id) {
        return boardroomMapper.toBoardroomResponseDto(boardroomRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Resource not found")));
    }


}
