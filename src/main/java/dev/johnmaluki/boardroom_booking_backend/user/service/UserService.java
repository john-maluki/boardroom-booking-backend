package dev.johnmaluki.boardroom_booking_backend.user.service;

import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserId(long userId);
}
