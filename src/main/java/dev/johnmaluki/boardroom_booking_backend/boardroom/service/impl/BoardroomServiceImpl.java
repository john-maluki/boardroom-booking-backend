package dev.johnmaluki.boardroom_booking_backend.boardroom.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockedBoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.mapper.BoardroomMapper;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.BoardroomRepository;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.LockedRoomRepository;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomService;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardroomServiceImpl implements BoardroomService {
    private final BoardroomRepository boardroomRepository;
    private final LockedRoomRepository lockedRoomRepository;
    private final BoardroomMapper boardroomMapper;
    @Override
    public List<BoardroomResponseDto> getAllBoardrooms() {
        return boardroomMapper.toBoardroomResponseDtoList(boardroomRepository.findAll());
    }

    @Override
    public BoardroomResponseDto getBoardroomById(long boardroomId) {
        return boardroomMapper.toBoardroomResponseDto(boardroomRepository.findById(boardroomId).orElseThrow(
                () -> new ResourceNotFoundException("Resource not found")));
    }

    @Override
    public LockedBoardroomResponseDto getLockedBoardroomReasonById(long boardroomId) {
        Optional<Boardroom> boardroomOptional = boardroomRepository.findById(boardroomId);
        if (boardroomOptional.isPresent()) {
            List<LockedRoom> lockedRooms = lockedRoomRepository.findAllByBoardroomAndLockedTrueOrderByCreatedAtDesc(
                    boardroomOptional.get());
            if (lockedRooms.isEmpty()) {
                throw new ResourceNotFoundException("Resource not found");
            } else {
                return boardroomMapper.toLockedBoardroomResponseDto(lockedRooms.get(0));
            }
        } else {
            throw new ResourceNotFoundException("Resource not found");
        }
    }


}
