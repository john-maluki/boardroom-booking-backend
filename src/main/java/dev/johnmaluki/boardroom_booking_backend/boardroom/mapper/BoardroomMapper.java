package dev.johnmaluki.boardroom_booking_backend.boardroom.mapper;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class BoardroomMapper {
    public BoardroomResponseDto toBoardroomResponseDto(Boardroom boardroom) {
        return BoardroomResponseDto.builder()
                .email(boardroom.getEmail())
                .name(boardroom.getName())
                .locked(boardroom.isLocked())
                .centre(boardroom.getCentre())
                .department(boardroom.getDepartment())
                .internetEnabled(boardroom.isInternetEnabled())
                .tag(boardroom.getTag())
                .picture(Base64.getEncoder().encodeToString(boardroom.getPicture()))
                .meetingTypeSupported(boardroom.getMeetingTypeSupported())
                .build();
    }

    public List<BoardroomResponseDto> toBoardroomResponseDtoList(List<Boardroom> boardrooms) {
        return  boardrooms.stream().map(this::toBoardroomResponseDto)
                .toList();
    }
}