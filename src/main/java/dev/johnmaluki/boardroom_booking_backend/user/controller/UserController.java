package dev.johnmaluki.boardroom_booking_backend.user.controller;

import dev.johnmaluki.boardroom_booking_backend.user.dto.*;
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

    @GetMapping("/system-administrators")
    @Operation(summary = "Fetch all system administrators")
    public ResponseEntity<List<SystemAdministratorResponseDto>> getSystemAdministrators() {
        return  ResponseEntity.ok(userService.getSystemAdministrators());
    }

    @PostMapping("/system-administrators")
    @Operation(summary = "Create system administrator")
    public ResponseEntity<SystemAdministratorResponseDto> createSystemAdministrator(
            @RequestBody @Valid SystemAdministratorDto systemAdministratorDto
    ) {
        return ResponseEntity.ok(userService.createSystemAdministrator(systemAdministratorDto));
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

    @DeleteMapping("/system-administrators/{adminId}")
    @Operation(summary = "Remove user as administrator")
    public ResponseEntity<Void> removeSystemAdmin(
            @PathVariable("adminId") long adminId
    ) {
        userService.removeSystemAdmin(adminId);
        return  ResponseEntity.noContent().build();
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
