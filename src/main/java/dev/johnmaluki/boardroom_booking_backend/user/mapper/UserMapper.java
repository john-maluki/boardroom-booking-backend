package dev.johnmaluki.boardroom_booking_backend.user.mapper;

import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {
    public UserResponseDto toUserResponseDto(AppUser user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .timeZone(user.getTimeZone())
                .username(user.getUsername())
                .department(user.getDepartment())
                .email(user.getEmail())
                .tag(user.getTag())
                .build();
    }

    public List<UserResponseDto> toUserResponseDtoList(List<AppUser> users) {
        return users.stream().map(this::toUserResponseDto).toList();
    }

}
