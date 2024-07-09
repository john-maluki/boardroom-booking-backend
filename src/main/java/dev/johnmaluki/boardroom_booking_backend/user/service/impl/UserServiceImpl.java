package dev.johnmaluki.boardroom_booking_backend.user.service.impl;

import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.mapper.UserMapper;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final AppUserRepository userRepository;
    private final UserMapper userMapper;
    @Override
    public List<UserResponseDto> getAllUsers() {
        return userMapper.toUserResponseDtoList(
                userRepository.findAll()
        );
    }

    @Override
    public UserResponseDto getUserId(long userId) {
        return userMapper.toUserResponseDto(
                userRepository.findById(userId).orElseThrow(
                        () -> new ResourceNotFoundException("Resource not found")
                )
        );
    }

    @Override
    public List<UserTimezoneResponseDto> getUserTimezones() {
        List<String> zoneIds = ZoneId.getAvailableZoneIds().stream().sorted().toList();
        return userMapper.toUserTimezoneResponseDtoList(zoneIds);
    }
}
