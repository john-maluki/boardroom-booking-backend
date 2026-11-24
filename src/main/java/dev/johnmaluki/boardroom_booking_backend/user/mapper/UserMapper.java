package dev.johnmaluki.boardroom_booking_backend.user.mapper;

import dev.johnmaluki.boardroom_booking_backend.user.dto.KemriEmployeeResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.SystemAdministratorDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.SystemAdministratorResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppAdmin;
import dev.johnmaluki.boardroom_booking_backend.user.view.AppUserView;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  /** Takes the joined user-plus-role view, since the role is its own row under R2DBC. */
  public UserResponseDto toUserResponseDto(AppUserView view) {
    return UserResponseDto.builder()
        .id(view.id())
        .timeZone(view.timeZone())
        .username(view.username())
        .fullName(view.fullName())
        .department(view.department())
        .role(view.authority().name())
        .email(view.email())
        .tag(view.tag())
        .build();
  }

  public UserTimezoneResponseDto toUserTimezoneResponseDto(String zoneId) {
    return UserTimezoneResponseDto.builder().timezone(zoneId).build();
  }

  public SystemAdministratorResponseDto toSystemAdministratorResponseDto(AppAdmin appAdmin) {
    return SystemAdministratorResponseDto.builder()
        .id(appAdmin.getId())
        .email(appAdmin.getEmail())
        .tag(appAdmin.getTag())
        .build();
  }

  public AppAdmin toAppAdmin(SystemAdministratorDto systemAdministratorDto) {
    return AppAdmin.builder().email(systemAdministratorDto.email()).build();
  }

  public KemriEmployeeResponseDto toKemriEmployeeResponseDto(Map<String, Object> employee) {
    return KemriEmployeeResponseDto.builder()
        .name((String) employee.get("Employee_Name"))
        .email((String) employee.get("Email"))
        .build();
  }
}
