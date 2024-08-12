package dev.johnmaluki.boardroom_booking_backend.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.user.dto.*;
import dev.johnmaluki.boardroom_booking_backend.user.mapper.UserMapper;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppAdmin;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppAdminRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.user.service.UserService;
import dev.johnmaluki.boardroom_booking_backend.user.service.UserServiceUtil;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserServiceUtil {
    private final AppUserRepository userRepository;
    private final AppAdminRepository appAdminRepository;
    private final UserMapper userMapper;
    private final CredentialsProvider credentialsProvider;
    private final Dotenv dotenv;

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userMapper.toUserResponseDtoList(
                userRepository.findAll()
        );
    }

    @Override
    public UserResponseDto getUserId(long userId) {
        return userMapper.toUserResponseDto(
                this.getUserFromDb(userId)
        );
    }

    @Override
    public List<UserTimezoneResponseDto> getUserTimezones() {
        List<String> zoneIds = ZoneId.getAvailableZoneIds().stream().sorted().toList();
        return userMapper.toUserTimezoneResponseDtoList(zoneIds);
    }

    @Override
    public UserResponseDto changeUserTimezone(long userId, UserTimezoneDto userTimezoneDto) {
        AppUser user = this.getUserFromDb(userId);
        user.setTimeZone(userTimezoneDto.userTimezone());
        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    public List<KemriEmployeeResponseDto> getKemriEmployees() {
        List<Map<String, Object>> employees = this.makeNTLMRequestToGetKemriEmployees();
        return userMapper.toKemriEmployeeResponseDtoList(employees);
    }

    @Override
    public List<SystemAdministratorResponseDto> getSystemAdministrators() {
        List<AppAdmin> appAdmins = appAdminRepository.findAllByArchivedFalseAndDeletedFalse();
        return userMapper.toSystemAdministratorResponseDtoList(appAdmins);
    }

    @Override
    public void removeSystemAdmin(long adminId) {
        AppAdmin appAdmin = this.findAdminById(adminId);
        appAdminRepository.delete(appAdmin);
    }

    private AppUser getUserFromDb(long userId){
        return userRepository.findByIdAndArchivedFalseAndDeletedFalse(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
    }

    @Override
    public List<AppUser> getAllSystemAdministrators() {
        return userRepository.findApplicationAdministrators();
    }

    private List<Map<String, Object>> makeNTLMRequestToGetKemriEmployees() {
        String targetUrl = dotenv.get("DEV_NAV_TARGET_URL");
        // Create HttpClient with credentials
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build()) {
            // Create the GET request
            HttpGet httpGet = new HttpGet(targetUrl);
            // Execute the request
            HttpResponse response = httpClient.execute(httpGet);
            // Get the response content
            String jsonResponse = EntityUtils.toString(response.getEntity());
            // Convert JSON response to Map
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> result = objectMapper.readValue(jsonResponse, Map.class);
            return (List<Map<String, Object>>) result.get("value");
        } catch (IOException e) {
            System.out.printf(e.getMessage());
        }
        return Collections.emptyList();
    }

    private AppAdmin findAdminById(long adminId) {
        return appAdminRepository.findById(adminId).orElseThrow(
                () -> new ResourceNotFoundException("Admin not found")
        );
    }
}
