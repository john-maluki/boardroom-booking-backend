package dev.johnmaluki.boardroom_booking_backend.boardroom.mapper;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockedBoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class BoardroomMapper {
    public BoardroomResponseDto toBoardroomResponseDto(Boardroom boardroom) {
        return BoardroomResponseDto.builder()
                .id(boardroom.getId())
                .capacity(boardroom.getCapacity())
                .email(boardroom.getEmail())
                .name(boardroom.getName())
                .locked(boardroom.isLocked())
                .centre(boardroom.getCentre())
                .department(boardroom.getDepartment())
                .internetEnabled(boardroom.isInternetEnabled())
                .tag(boardroom.getTag())
                .description(boardroom.getDescription())
                .picture(Base64.getEncoder().encodeToString(boardroom.getPicture()))
                .meetingTypeSupported(boardroom.getMeetingTypeSupported())
                .hasOngoingMeeting(boardroom.isHasOngoingMeeting())
                .build();
    }

    public Boardroom toBoardroom(BoardroomDto boardroomDto) {
        return Boardroom.builder()
                .capacity(boardroomDto.capacity())
                .email(boardroomDto.email())
                .name(boardroomDto.name())
                .centre(boardroomDto.centre())
                .department(boardroomDto.department())
                .description(boardroomDto.description())
                .internetEnabled(boardroomDto.internetEnabled())
                .picture(Base64.getDecoder().decode(boardroomDto.description()))
                .meetingTypeSupported(boardroomDto.meetingTypeSupported())
                .build();
    }

    public List<BoardroomResponseDto> toBoardroomResponseDtoList(List<Boardroom> boardrooms) {
        return  boardrooms.stream().map(this::toBoardroomResponseDto)
                .toList();
    }

    public LockedBoardroomResponseDto toLockedBoardroomResponseDto(LockedRoom lockedRoom){
        return LockedBoardroomResponseDto.builder()
                .boardroomId(lockedRoom.getBoardroom().getId())
                .givenReason(lockedRoom.getGivenReason())
                .build();
    }
}
