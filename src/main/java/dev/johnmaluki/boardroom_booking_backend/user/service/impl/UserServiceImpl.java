package dev.johnmaluki.boardroom_booking_backend.user.service.impl;

import tools.jackson.databind.ObjectMapper;
import dev.johnmaluki.boardroom_booking_backend.core.cache.CacheKeys;
import dev.johnmaluki.boardroom_booking_backend.core.cache.CacheTtl;
import dev.johnmaluki.boardroom_booking_backend.core.cache.ReactiveCacheService;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.UniqueContraintException;
import dev.johnmaluki.boardroom_booking_backend.user.dto.KemriEmployeeResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.SystemAdministratorDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.SystemAdministratorResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneDto;
import dev.johnmaluki.boardroom_booking_backend.user.dto.UserTimezoneResponseDto;
import dev.johnmaluki.boardroom_booking_backend.user.mapper.UserMapper;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppAdminRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.UserQueryRepository;
import dev.johnmaluki.boardroom_booking_backend.user.service.UserService;
import dev.johnmaluki.boardroom_booking_backend.user.service.UserServiceUtil;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserServiceUtil {
  private final AppUserRepository userRepository;
  private final UserQueryRepository userQueryRepository;
  private final AppAdminRepository appAdminRepository;
  private final UserMapper userMapper;
  private final CredentialsProvider credentialsProvider;
  private final Dotenv dotenv;
  private final ObjectMapper objectMapper;
  private final ReactiveCacheService cacheService;

  @Override
  public Flux<UserResponseDto> getAllUsers() {
    return cacheService.cacheList(
        CacheKeys.USER_ALL,
        CacheTtl.REFERENCE_DATA,
        UserResponseDto.class,
        userQueryRepository.findAllActive().map(userMapper::toUserResponseDto));
  }

  @Override
  public Mono<UserResponseDto> getUserId(long userId) {
    return cacheService.cache(
        CacheKeys.userById(userId),
        CacheTtl.REFERENCE_DATA,
        UserResponseDto.class,
        userQueryRepository
            .findActiveById(userId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
            .map(userMapper::toUserResponseDto));
  }

  /**
   * The available zone ids come from the JDK, so this is cached with a long expiry purely to avoid
   * rebuilding and re-serialising a 600-entry list on every call.
   */
  @Override
  public Flux<UserTimezoneResponseDto> getUserTimezones() {
    return cacheService.cacheList(
        CacheKeys.USER_TIMEZONES,
        CacheTtl.STATIC_DATA,
        UserTimezoneResponseDto.class,
        Flux.fromIterable(ZoneId.getAvailableZoneIds().stream().sorted().toList())
            .map(userMapper::toUserTimezoneResponseDto));
  }

  @Override
  @Transactional
  public Mono<UserResponseDto> changeUserTimezone(long userId, UserTimezoneDto userTimezoneDto) {
    return userRepository
        .findByIdAndArchivedFalseAndDeletedFalse(userId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
        .flatMap(
            user -> {
              user.setTimeZone(userTimezoneDto.userTimezone());
              return userRepository.save(user);
            })
        .then(cacheService.evictByPattern(CacheKeys.USER_NAMESPACE))
        // Re-read through the role join so the response carries the user's authority.
        .then(userQueryRepository.findActiveById(userId))
        .map(userMapper::toUserResponseDto);
  }

  @Override
  public Flux<SystemAdministratorResponseDto> getSystemAdministrators() {
    return cacheService.cacheList(
        CacheKeys.USER_SYSTEM_ADMINS,
        CacheTtl.REFERENCE_DATA,
        SystemAdministratorResponseDto.class,
        appAdminRepository
            .findAllByArchivedFalseAndDeletedFalse()
            .map(userMapper::toSystemAdministratorResponseDto));
  }

  @Override
  @Transactional
  public Mono<Void> removeSystemAdmin(long adminId) {
    return appAdminRepository
        .findById(adminId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Admin not found")))
        .flatMap(appAdminRepository::delete)
        .then(cacheService.evictByPattern(CacheKeys.USER_NAMESPACE));
  }

  @Override
  @Transactional
  public Mono<SystemAdministratorResponseDto> createSystemAdministrator(
      SystemAdministratorDto systemAdministratorDto) {
    return appAdminRepository
        .existsByEmail(systemAdministratorDto.email())
        .defaultIfEmpty(false)
        .flatMap(
            exists ->
                Boolean.TRUE.equals(exists)
                    ? Mono.<SystemAdministratorResponseDto>error(
                        new UniqueContraintException("Admin with that email exists"))
                    : appAdminRepository
                        .save(userMapper.toAppAdmin(systemAdministratorDto))
                        .map(userMapper::toSystemAdministratorResponseDto))
        .flatMap(
            dto -> cacheService.evictByPattern(CacheKeys.USER_NAMESPACE).thenReturn(dto));
  }

  @Override
  public Flux<AppUser> getAllSystemAdministrators() {
    return userRepository.findApplicationAdministrators();
  }

  @Override
  public Mono<String> findEmailByUserId(long userId) {
    return userRepository
        .findByIdAndArchivedFalseAndDeletedFalse(userId)
        .mapNotNull(AppUser::getEmail);
  }

  /**
   * Navision employee directory.
   *
   * <p>This call is NTLM-authenticated, which neither {@code WebClient} nor Reactor Netty supports,
   * so Apache HttpClient stays. It is blocking, so it runs on {@code boundedElastic} rather than on
   * an event loop. Failures yield an empty list, as before.
   */
  @Override
  public Flux<KemriEmployeeResponseDto> getKemriEmployees() {
    return Mono.fromCallable(this::makeNtlmRequestToGetKemriEmployees)
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable)
        .map(userMapper::toKemriEmployeeResponseDto);
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> makeNtlmRequestToGetKemriEmployees() {
    String targetUrl = dotenv.get("DEV_NAV_TARGET_URL");
    try (CloseableHttpClient httpClient =
        HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build()) {
      HttpResponse response = httpClient.execute(new HttpGet(targetUrl));
      String jsonResponse = EntityUtils.toString(response.getEntity());
      Map<String, Object> result = objectMapper.readValue(jsonResponse, Map.class);
      Object value = result.get("value");
      return value == null ? Collections.emptyList() : (List<Map<String, Object>>) value;
    } catch (IOException e) {
      log.error("Could not fetch KEMRI employees from Navision: {}", e.getMessage());
      return Collections.emptyList();
    }
  }
}
