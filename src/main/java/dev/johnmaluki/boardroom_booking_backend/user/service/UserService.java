package dev.johnmaluki.boardroom_booking_backend.user.service;

import dev.johnmaluki.boardroom_booking_backend.user.dto.KemriEmployeeResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneResponseDto;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserId(long userId);
    List<UserTimezoneResponseDto> getUserTimezones();
    UserResponseDto changeUserTimezone(long userId, UserTimezoneDto userTimezoneDto);
    List<KemriEmployeeResponseDto> getKemriEmployees();
}
