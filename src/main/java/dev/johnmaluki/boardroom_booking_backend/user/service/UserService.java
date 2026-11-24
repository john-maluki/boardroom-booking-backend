package dev.johnmaluki.boardroom_booking_backend.user.service;

import dev.johnmaluki.boardroom_booking_backend.user.dto.KemriEmployeeResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.SystemAdministratorDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.SystemAdministratorResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
  Flux<UserResponseDto> getAllUsers();

  Mono<UserResponseDto> getUserId(long userId);

  Flux<UserTimezoneResponseDto> getUserTimezones();

  Mono<UserResponseDto> changeUserTimezone(long userId, UserTimezoneDto userTimezoneDto);

  Flux<KemriEmployeeResponseDto> getKemriEmployees();

  Flux<SystemAdministratorResponseDto> getSystemAdministrators();

  Mono<Void> removeSystemAdmin(long adminId);

  Mono<SystemAdministratorResponseDto> createSystemAdministrator(
      SystemAdministratorDto systemAdministratorDto);
}
