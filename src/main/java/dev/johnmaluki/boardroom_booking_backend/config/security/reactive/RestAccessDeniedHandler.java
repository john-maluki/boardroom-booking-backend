package dev.johnmaluki.boardroom_booking_backend.config.security.reactive;

import dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler.JsonErrorResponseWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * What an authenticated request without the required authority gets back — a USER token hitting
 * an ADMIN-only path, for example.
 *
 * <p>Same motivation as {@link RestAuthenticationEntryPoint}: Spring Security's own default here
 * is a bare 403 with no body, inconsistent with every other error response in the API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestAccessDeniedHandler implements ServerAccessDeniedHandler {
  private final JsonErrorResponseWriter responseWriter;

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException exception) {
    log.debug("Rejecting unauthorized request to {}: {}", exchange.getRequest().getPath(), exception.getMessage());
    return responseWriter.write(
        exchange, HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
  }
}
