package dev.johnmaluki.boardroom_booking_backend.user.service;

import dev.johnmaluki.boardroom_booking_backend.user.dto.*;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserId(long userId);
    List<UserTimezoneResponseDto> getUserTimezones();
    UserResponseDto changeUserTimezone(long userId, UserTimezoneDto userTimezoneDto);
    List<KemriEmployeeResponseDto> getKemriEmployees();
    List<SystemAdministratorResponseDto> getSystemAdministrators();
}
