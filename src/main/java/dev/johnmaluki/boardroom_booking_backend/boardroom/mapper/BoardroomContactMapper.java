package dev.johnmaluki.boardroom_booking_backend.boardroom.mapper;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.BoardroomContact;
import org.springframework.stereotype.Component;

@Component
public class BoardroomContactMapper {

  public BoardroomContactResponseDto toBoardroomContactResponseDto(BoardroomContact contact) {
    return BoardroomContactResponseDto.builder()
        .id(contact.getId())
        .contact(contact.getContact())
        .boardroomId(contact.getBoardroomId())
        .tag(contact.getTag())
        .build();
  }

  public BoardroomContact toBoardroomContact(BoardroomContactDto boardroomContactDto) {
    return BoardroomContact.builder().contact(boardroomContactDto.contact()).build();
  }
}
