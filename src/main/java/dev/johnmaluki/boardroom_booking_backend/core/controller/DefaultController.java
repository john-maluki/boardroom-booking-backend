package dev.johnmaluki.boardroom_booking_backend.core.controller;

import io.swagger.v3.oas.annotations.Hidden;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Hidden
public class DefaultController {

  /** WebFlux has no {@code RedirectView}, so the redirect is written to the response directly. */
  @GetMapping("/")
  public Mono<Void> redirectToDefault(ServerHttpResponse response) {
    response.setStatusCode(HttpStatus.FOUND);
    response.getHeaders().setLocation(URI.create("/swagger-ui/index.html"));
    return response.setComplete();
  }
}
