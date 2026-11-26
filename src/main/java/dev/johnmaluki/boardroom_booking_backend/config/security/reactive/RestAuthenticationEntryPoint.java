package dev.johnmaluki.boardroom_booking_backend.config.security.reactive;

import dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler.JsonErrorResponseWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * What a request with no usable authentication gets back: a missing, malformed, or expired
 * bearer token, or one naming a user that no longer exists.
 *
 * <p>Without this, Spring Security's own default entry point returns a bare 401 with no body at
 * all — the single most common failure mode a token-based API's clients will hit, and the one
 * place in this application that was returning a response shaped nothing like the rest of it.
 * {@code exception.getMessage()} is deliberately not forwarded: Spring Security's messages here
 * are usually harmless ("Full authentication is required...") but are not written with a client
 * audience in mind, so a fixed, equally clear message is used instead and the real one is logged.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
  private final JsonErrorResponseWriter responseWriter;

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException exception) {
    log.debug("Rejecting unauthenticated request to {}: {}", exchange.getRequest().getPath(), exception.getMessage());
    return responseWriter.write(
        exchange,
        HttpStatus.UNAUTHORIZED,
        "Authentication required: missing, invalid, or expired token.");
  }
}
