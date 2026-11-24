package dev.johnmaluki.boardroom_booking_backend.auth.controller;

import dev.johnmaluki.boardroom_booking_backend.auth.dto.LoginRequestDto;
import dev.johnmaluki.boardroom_booking_backend.auth.dto.RefreshTokenRequestDto;
import dev.johnmaluki.boardroom_booking_backend.auth.dto.TokenResponseDto;
import dev.johnmaluki.boardroom_booking_backend.auth.service.AuthService;
import dev.johnmaluki.boardroom_booking_backend.core.service.ProfileChecker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {
  private static final Duration ACCESS_COOKIE_MAX_AGE = Duration.ofSeconds(5000);

  private final AuthService authService;
  private final ProfileChecker profileChecker;

  @PostMapping("/auth/login")
  @Operation(summary = "Authenticate against the directory and issue tokens")
  public Mono<TokenResponseDto> login(
      @RequestBody @Valid LoginRequestDto request, ServerHttpResponse response) {
    return authService
        .login(request)
        .doOnNext(tokens -> response.addCookie(accessTokenCookie(tokens.accessToken())));
  }

  @PostMapping("/auth/refresh-token")
  @Operation(summary = "Exchange a refresh token for a new access token")
  public Mono<TokenResponseDto> refresh(@RequestBody @Valid RefreshTokenRequestDto request) {
    return authService.refresh(request);
  }

  /**
   * The access token is also set as an http-only cookie. The browser's {@code EventSource} cannot
   * attach an {@code Authorization} header, so the Server-Sent Events endpoint authenticates from
   * this cookie; every other endpoint continues to use the bearer header.
   *
   * <p>{@code Secure} is dropped when running locally, because a secure cookie is not stored over
   * plain {@code http://localhost} and the dev SSE stream would never authenticate.
   */
  private ResponseCookie accessTokenCookie(String accessToken) {
    return ResponseCookie.from("access_token", accessToken)
        .httpOnly(true)
        .secure(!profileChecker.checkIfAppRunningLocally())
        .path("/")
        .maxAge(ACCESS_COOKIE_MAX_AGE)
        .sameSite("Lax")
        .build();
  }
}
