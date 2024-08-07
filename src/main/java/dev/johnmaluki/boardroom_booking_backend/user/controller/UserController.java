package dev.johnmaluki.boardroom_booking_backend.user.controller;

import dev.johnmaluki.boardroom_booking_backend.user.dto.KemriEmployeeResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return  ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/kemri-employees")
    @Operation(summary = "Fetch all kemri employees")
    public ResponseEntity<List<KemriEmployeeResponseDto>> getKemriEmployees() {
        return  ResponseEntity.ok(userService.getKemriEmployees());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUserId(
            @PathVariable("userId") long userId
    ) {
        return  ResponseEntity.ok(userService.getUserId(userId));
    }

    @GetMapping("/user-timezones")
    @Operation(summary = "Fetch user timezones")
    public ResponseEntity<List<UserTimezoneResponseDto>> getUserTimezones() {
        return  ResponseEntity.ok(userService.getUserTimezones());
    }

    @PatchMapping("/users/{userId}")
    @Operation(summary = "Update user timezone")
    public ResponseEntity<UserResponseDto> updateUserTimezone(
            @PathVariable("userId") long userId,
            @RequestBody @Valid UserTimezoneDto userTimezoneDto
    ) {
        return  ResponseEntity.ok(userService.changeUserTimezone(userId, userTimezoneDto));
    }

}
