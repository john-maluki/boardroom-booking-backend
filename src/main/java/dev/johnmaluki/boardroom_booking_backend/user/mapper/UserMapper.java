package dev.johnmaluki.boardroom_booking_backend.user.mapper;

import dev.johnmaluki.boardroom_booking_backend.user.dto.*;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppAdmin;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UserMapper {
    public UserResponseDto toUserResponseDto(AppUser user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .timeZone(user.getTimeZone())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .department(user.getDepartment())
                .role(user.getRole().getAuthority().name())
                .email(user.getEmail())
                .tag(user.getTag())
                .build();
    }

    public List<UserResponseDto> toUserResponseDtoList(List<AppUser> users) {
        return users.stream().map(this::toUserResponseDto).toList();
    }

    public List<UserTimezoneResponseDto> toUserTimezoneResponseDtoList(List<String> zoneIds) {
        return zoneIds.stream().map(
                z -> UserTimezoneResponseDto.builder()
                        .timezone(z)
                        .build()
        ).toList();
    }

    public SystemAdministratorResponseDto toSystemAdministratorResponseDto(AppAdmin appAdmin) {
        return SystemAdministratorResponseDto.builder()
                .id(appAdmin.getId())
                .email(appAdmin.getEmail())
                .tag(appAdmin.getTag())
                .build();
    }

    public AppAdmin toAppAdmin(SystemAdministratorDto systemAdministratorDto) {
        return  AppAdmin.builder()
                .email(systemAdministratorDto.email())
                .build();
    }

    public List<SystemAdministratorResponseDto> toSystemAdministratorResponseDtoList(List<AppAdmin> appAdmins) {
        return appAdmins.stream().map(this::toSystemAdministratorResponseDto).toList();
    }

    public List<KemriEmployeeResponseDto> toKemriEmployeeResponseDtoList(List<Map<String, Object>> employees) {
        return employees.stream().map(this::toKemriEmployeeResponseDto).toList();
    }

    private KemriEmployeeResponseDto toKemriEmployeeResponseDto(Map<String, Object> employee) {
        return KemriEmployeeResponseDto.builder()
                .name((String) employee.get("Employee_Name"))
                .email((String) employee.get("Email"))
                .build();
    }

}
