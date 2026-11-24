package dev.johnmaluki.boardroom_booking_backend.user.controller;

import dev.johnmaluki.boardroom_booking_backend.user.dto.KemriEmployeeResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.SystemAdministratorDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.SystemAdministratorResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {
  private final UserService userService;

  @GetMapping("/users")
  public Flux<UserResponseDto> getAllUsers() {
    return userService.getAllUsers();
  }

  @GetMapping("/system-administrators")
  @Operation(summary = "Fetch all system administrators")
  public Flux<SystemAdministratorResponseDto> getSystemAdministrators() {
    return userService.getSystemAdministrators();
  }

  @PostMapping("/system-administrators")
  @Operation(summary = "Create system administrator")
  public Mono<SystemAdministratorResponseDto> createSystemAdministrator(
      @RequestBody @Valid SystemAdministratorDto systemAdministratorDto) {
    return userService.createSystemAdministrator(systemAdministratorDto);
  }

  @GetMapping("/kemri-employees")
  @Operation(summary = "Fetch all kemri employees")
  public Flux<KemriEmployeeResponseDto> getKemriEmployees() {
    return userService.getKemriEmployees();
  }

  @GetMapping("/users/{userId}")
  public Mono<UserResponseDto> getUserId(@PathVariable("userId") long userId) {
    return userService.getUserId(userId);
  }

  @DeleteMapping("/system-administrators/{adminId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Remove user as administrator")
  public Mono<Void> removeSystemAdmin(@PathVariable("adminId") long adminId) {
    return userService.removeSystemAdmin(adminId);
  }

  @GetMapping("/user-timezones")
  @Operation(summary = "Fetch user timezones")
  public Flux<UserTimezoneResponseDto> getUserTimezones() {
    return userService.getUserTimezones();
  }

  @PatchMapping("/users/{userId}")
  @Operation(summary = "Update user timezone")
  public Mono<UserResponseDto> updateUserTimezone(
      @PathVariable("userId") long userId, @RequestBody @Valid UserTimezoneDto userTimezoneDto) {
    return userService.changeUserTimezone(userId, userTimezoneDto);
  }
}
