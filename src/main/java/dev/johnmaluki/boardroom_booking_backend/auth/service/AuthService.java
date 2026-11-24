package dev.johnmaluki.boardroom_booking_backend.auth.service;

import dev.johnmaluki.boardroom_booking_backend.auth.dto.LoginRequestDto;
import dev.johnmaluki.boardroom_booking_backend.auth.dto.RefreshTokenRequestDto;
import dev.johnmaluki.boardroom_booking_backend.auth.dto.TokenResponseDto;
import dev.johnmaluki.boardroom_booking_backend.config.security.JwtService;
import dev.johnmaluki.boardroom_booking_backend.config.security.LdapService;
import dev.johnmaluki.boardroom_booking_backend.config.security.LdapUserDetail;
import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import dev.johnmaluki.boardroom_booking_backend.config.security.reactive.PrincipalLoader;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppAdminRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.RoleRepository;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Login and token refresh.
 *
 * <p>This replaces the two servlet filters that used to do the job. Authentication as a filter made
 * sense when the credentials had to be read off a blocking {@code InputStream}; as a plain endpoint
 * it gains request validation, a documented contract in the OpenAPI output, and a pipeline Spring
 * can compose, while the request and response bodies stay byte-for-byte what clients already send
 * and expect.
 *
 * <p>User provisioning happens once here rather than inside each directory implementation. A
 * successful bind is the source of truth for the user's name and email, and membership of the
 * {@code administrators} table is what grants the ADMIN authority — both are re-applied on every
 * login so revoking an administrator takes effect at their next sign-in.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private final LdapService ldapService;
  private final JwtService jwtService;
  private final AppUserRepository userRepository;
  private final AppAdminRepository appAdminRepository;
  private final RoleRepository roleRepository;
  private final PrincipalLoader principalLoader;
  private final UserUtil userUtil;

  /**
   * Not transactional as a whole: the directory bind is a blocking network round trip, and holding
   * a database connection open across it would tie up the pool for the length of someone else's
   * LDAP latency. Only the provisioning writes run in a transaction.
   */
  public Mono<TokenResponseDto> login(LoginRequestDto request) {
    return ldapService
        .authenticate(request.username(), request.password())
        .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")))
        .flatMap(this::provisionUser)
        .map(this::issueTokens);
  }

  /**
   * Issues a fresh access token from a still-valid refresh token. The refresh token itself is
   * returned unchanged, matching the previous behaviour.
   */
  public Mono<TokenResponseDto> refresh(RefreshTokenRequestDto request) {
    String refreshToken = request.refreshToken();
    return Mono.fromCallable(() -> jwtService.extractUsername(refreshToken))
        .flatMap(principalLoader::loadByUsername)
        .filter(principal -> jwtService.isTokenValid(refreshToken, principal.getUsername()))
        .switchIfEmpty(Mono.error(new BadCredentialsException("Refresh token is not valid")))
        .map(
            principal ->
                TokenResponseDto.builder()
                    .accessToken(jwtService.generateToken(principal.getUsername(), claims(principal)))
                    .refreshToken(refreshToken)
                    .build());
  }

  @Transactional
  public Mono<UserPrincipal> provisionUser(LdapUserDetail detail) {
    Mono<Boolean> isSystemAdmin =
        detail.email() == null
            ? Mono.just(false)
            : appAdminRepository.existsByEmail(detail.email()).defaultIfEmpty(false);

    return isSystemAdmin.flatMap(
        admin ->
            userRepository
                .findByUsername(detail.username())
                .flatMap(existing -> updateUser(existing, detail, admin))
                .switchIfEmpty(Mono.defer(() -> createUser(detail, admin))));
  }

  private Mono<UserPrincipal> updateUser(AppUser user, LdapUserDetail detail, boolean admin) {
    user.setUsername(detail.username());
    if (detail.email() != null) {
      user.setEmail(detail.email());
    }
    if (detail.name() != null) {
      user.setFullName(detail.name());
    }
    RoleType authority = admin ? RoleType.ADMIN : RoleType.USER;
    return userRepository
        .save(user)
        .flatMap(saved -> applyRole(saved, authority).thenReturn(new UserPrincipal(saved, authority)));
  }

  private Mono<UserPrincipal> createUser(LdapUserDetail detail, boolean admin) {
    RoleType authority = admin ? RoleType.ADMIN : RoleType.USER;
    AppUser user =
        AppUser.builder()
            .username(detail.username())
            .email(detail.email())
            // full_name is NOT NULL; the embedded dev directory has no display name to offer.
            .fullName(detail.name() == null ? detail.username() : detail.name())
            .timeZone(userUtil.getDefaultUserTimeZone())
            .build();
    return userRepository
        .save(user)
        .flatMap(
            saved ->
                roleRepository
                    .save(Role.builder().authority(authority).userId(saved.getId()).build())
                    .thenReturn(new UserPrincipal(saved, authority)));
  }

  private Mono<Role> applyRole(AppUser user, RoleType authority) {
    return roleRepository
        .findByUserId(user.getId())
        .flatMap(
            role -> {
              if (role.getAuthority() == authority) {
                return Mono.just(role);
              }
              role.setAuthority(authority);
              return roleRepository.save(role);
            })
        .switchIfEmpty(
            Mono.defer(
                () ->
                    roleRepository.save(
                        Role.builder().authority(authority).userId(user.getId()).build())));
  }

  private TokenResponseDto issueTokens(UserPrincipal principal) {
    return TokenResponseDto.builder()
        .accessToken(jwtService.generateToken(principal.getUsername(), claims(principal)))
        .refreshToken(jwtService.generateRefreshToken(principal.getUsername()))
        .build();
  }

  private Map<String, Object> claims(UserPrincipal principal) {
    AppUser user = principal.getUser();
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", principal.getAuthority());
    claims.put("fullName", user.getFullName());
    claims.put("email", user.getEmail());
    claims.put("timezone", user.getTimeZone());
    claims.put("id", user.getId());
    return claims;
  }
}
