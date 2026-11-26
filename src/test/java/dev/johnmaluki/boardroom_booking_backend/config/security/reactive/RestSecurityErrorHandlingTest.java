package dev.johnmaluki.boardroom_booking_backend.config.security.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler.JsonErrorResponseWriter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

/**
 * Proves that a request Spring Security rejects before it ever reaches a controller still gets a
 * body shaped like every other error in the API, rather than Spring Security's own bare, empty
 * 401/403 — the gap these two classes exist to close. {@code @ExceptionHandler} cannot be tested
 * against this path at all: {@code ExceptionTranslationWebFilter} is what calls these two types,
 * earlier in the filter chain than any controller dispatch.
 *
 * <p>The body assertions match {@code "<status-code> <status-phrase>"} — e.g. {@code "401
 * UNAUTHORIZED"} — because that is how Jackson actually serialises an {@code HttpStatus} field,
 * confirmed by inspecting a real written response rather than assumed from the field's type.
 */
class RestSecurityErrorHandlingTest {

  private final JsonErrorResponseWriter responseWriter =
      new JsonErrorResponseWriter(JsonMapper.builder().build());
  private final RestAuthenticationEntryPoint entryPoint =
      new RestAuthenticationEntryPoint(responseWriter);
  private final RestAccessDeniedHandler accessDeniedHandler =
      new RestAccessDeniedHandler(responseWriter);

  @Test
  void unauthenticatedRequestGetsA401WithAJsonBody() {
    ServerWebExchange exchange = exchange();

    Mono<Void> result =
        entryPoint.commence(exchange, new BadCredentialsException("token rejected internally"));

    StepVerifier.create(result).verifyComplete();
    assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    String body = writtenBody(exchange);
    assertTrue(body.contains("\"httpStatus\":\"401 UNAUTHORIZED\""));
    // The internal detail must never surface in the body, only ever in the server log.
    assertFalse(body.contains("rejected internally"));
  }

  @Test
  void underPrivilegedRequestGetsA403WithAJsonBody() {
    ServerWebExchange exchange = exchange();

    Mono<Void> result =
        accessDeniedHandler.handle(exchange, new AccessDeniedException("missing ADMIN authority"));

    StepVerifier.create(result).verifyComplete();
    assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    String body = writtenBody(exchange);
    assertTrue(body.contains("\"httpStatus\":\"403 FORBIDDEN\""));
    assertFalse(body.contains("missing ADMIN authority"));
  }

  private ServerWebExchange exchange() {
    return MockServerWebExchange.from(MockServerHttpRequest.get("/boardrooms").build());
  }

  /** The mock response buffers every write, so the body can be read back for assertions. */
  private String writtenBody(ServerWebExchange exchange) {
    return ((MockServerHttpResponse) exchange.getResponse()).getBodyAsString().block();
  }
}
