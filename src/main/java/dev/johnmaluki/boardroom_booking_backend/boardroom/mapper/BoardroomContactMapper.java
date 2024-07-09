package dev.johnmaluki.boardroom_booking_backend.boardroom.mapper;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomContactResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.BoardroomContact;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BoardroomContactMapper {
    public BoardroomContactResponseDto toBoardroomContactResponseDto(BoardroomContact boardroomContact) {
        return BoardroomContactResponseDto.builder()
                .id(boardroomContact.getId())
                .contact(boardroomContact.getContact())
                .boardroomId(boardroomContact.getBoardroom().getId())
                .tag(boardroomContact.getTag())
                .build();
    }

    public List<BoardroomContactResponseDto> toBoardroomContactResponseDtoList(List<BoardroomContact> boardroomContacts) {
        return boardroomContacts.stream().map(this::toBoardroomContactResponseDto).toList();
    }

    public BoardroomContact toBoardroomContact(BoardroomContactDto boardroomContactDto) {
        return BoardroomContact.builder()
                .contact(boardroomContactDto.contact())
                .build();
    }
}
