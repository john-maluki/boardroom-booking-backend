package dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler;

import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceErrorMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

/**
 * Writes a {@link ResourceErrorMessage} body directly to a {@link ServerWebExchange}.
 *
 * <p>{@code @ExceptionHandler} methods only run for exceptions raised while a request is being
 * dispatched to a controller. An authentication or authorization failure never gets that far —
 * {@code ExceptionTranslationWebFilter} catches it earlier, in the security filter chain, and
 * hands it to a {@code ServerAuthenticationEntryPoint} or {@code ServerAccessDeniedHandler}
 * instead, neither of which can return a {@code ResponseEntity}. This is the shared plumbing that
 * lets both of those still produce the same response shape as every other error in the API,
 * rather than Spring Security's own bare, empty-bodied default.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonErrorResponseWriter {
  private final ObjectMapper objectMapper;

  public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    byte[] body = serialise(message, status);
    return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
  }

  private byte[] serialise(String message, HttpStatus status) {
    try {
      return objectMapper.writeValueAsBytes(
          ResourceErrorMessage.builder().message(message).httpStatus(status).build());
    } catch (RuntimeException e) {
      // Jackson 3's writeValueAsBytes throws an unchecked JacksonException on failure; falling
      // back to a hand-built literal means a serialisation problem still returns a readable
      // error instead of this writer silently producing an empty body.
      log.warn("Could not serialise error response; falling back to a literal body", e);
      String escaped = message.replace("\"", "'");
      return ("{\"message\":\"" + escaped + "\",\"httpStatus\":\"" + status.name() + "\"}")
          .getBytes(StandardCharsets.UTF_8);
    }
  }
}
