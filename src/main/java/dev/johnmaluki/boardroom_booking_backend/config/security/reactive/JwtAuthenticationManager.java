package dev.johnmaluki.boardroom_booking_backend.config.security.reactive;

import dev.johnmaluki.boardroom_booking_backend.config.security.JwtService;
import dev.johnmaluki.boardroom_booking_backend.core.exception.JwtTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Verifies the bearer token and exchanges it for a fully populated authentication.
 *
 * <p>This is the reactive replacement for the old {@code OncePerRequestFilter}: instead of mutating
 * a thread-local {@code SecurityContextHolder}, it returns an {@code Authentication} that Spring
 * Security places in the Reactor context for the rest of the request.
 *
 * <p>A token that is missing, malformed, expired or names a user who no longer exists yields an
 * empty {@code Mono}, which leaves the request anonymous and lets the authorisation rules reject it
 * with a 401. Rejecting here instead would turn every unauthenticated probe into an error log.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
  private final JwtService jwtService;
  private final PrincipalLoader principalLoader;

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    String token = String.valueOf(authentication.getCredentials());
    return Mono.fromCallable(() -> jwtService.extractUsername(token))
        .onErrorResume(
            JwtTokenException.class,
            error -> {
              log.debug("Rejecting bearer token: {}", error.getMessage());
              return Mono.empty();
            })
        .flatMap(principalLoader::loadByUsername)
        .filter(principal -> jwtService.isTokenValid(token, principal.getUsername()))
        .map(
            principal ->
                (Authentication)
                    new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
  }
}
