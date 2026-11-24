package dev.johnmaluki.boardroom_booking_backend.boardroom.mapper;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomEventFilterResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.LockedBoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Contacts are now passed in rather than read off the entity, because R2DBC does not populate
 * collections. Listing endpoints pass an empty list, matching what the JPA version serialised for a
 * boardroom whose contacts were never initialised.
 */
@Component
public class BoardroomMapper {

  public BoardroomResponseDto toBoardroomResponseDto(
      Boardroom boardroom, List<BoardroomContactResponseDto> contacts) {
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
        .picture(boardroom.getPicture())
        .meetingTypeSupported(boardroom.getMeetingTypeSupported())
        .hasOngoingMeeting(boardroom.isHasOngoingMeeting())
        .boardroomContacts(contacts == null ? List.of() : contacts)
        .build();
  }

  public BoardroomResponseDto toBoardroomResponseDto(Boardroom boardroom) {
    return toBoardroomResponseDto(boardroom, List.of());
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
        .picture(boardroomDto.picture())
        .meetingTypeSupported(boardroomDto.meetingTypeSupported())
        .build();
  }

  public LockedBoardroomResponseDto toLockedBoardroomResponseDto(LockedRoom lockedRoom) {
    return LockedBoardroomResponseDto.builder()
        .boardroomId(lockedRoom.getBoardroomId())
        .givenReason(lockedRoom.getGivenReason())
        .build();
  }

  public BoardroomEventFilterResponseDto toBoardroomEventFilterResponseDto(Boardroom boardroom) {
    return BoardroomEventFilterResponseDto.builder()
        .id(boardroom.getId())
        .boardroomName(boardroom.getName())
        .tag(boardroom.getTag())
        .build();
  }
}
