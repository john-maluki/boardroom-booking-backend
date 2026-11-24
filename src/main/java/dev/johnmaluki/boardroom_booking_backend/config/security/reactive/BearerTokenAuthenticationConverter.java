package dev.johnmaluki.boardroom_booking_backend.config.security.reactive;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Extracts the access token from a request so {@link JwtAuthenticationManager} can verify it.
 *
 * <p>The {@code Authorization: Bearer} header is the primary source and is what every normal API
 * call uses. The {@code access_token} cookie set at login is accepted as a fallback because the
 * browser's {@code EventSource} cannot send custom headers, and the Server-Sent Events endpoint
 * would otherwise be unreachable from a browser.
 *
 * <p>Requests carrying neither produce nothing and stay anonymous, leaving the authorisation rules
 * to decide the response.
 */
@Component
public class BearerTokenAuthenticationConverter implements ServerAuthenticationConverter {
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ACCESS_TOKEN_COOKIE = "access_token";

  @Override
  public Mono<Authentication> convert(ServerWebExchange exchange) {
    String token = tokenFromHeader(exchange);
    if (token == null) {
      token = tokenFromCookie(exchange);
    }
    return token == null
        ? Mono.empty()
        : Mono.just(new UsernamePasswordAuthenticationToken(token, token));
  }

  private String tokenFromHeader(ServerWebExchange exchange) {
    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      return null;
    }
    return blankToNull(authHeader.substring(BEARER_PREFIX.length()));
  }

  private String tokenFromCookie(ServerWebExchange exchange) {
    HttpCookie cookie = exchange.getRequest().getCookies().getFirst(ACCESS_TOKEN_COOKIE);
    return cookie == null ? null : blankToNull(cookie.getValue());
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
