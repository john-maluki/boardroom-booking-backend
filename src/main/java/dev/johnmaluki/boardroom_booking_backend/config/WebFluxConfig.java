package dev.johnmaluki.boardroom_booking_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * CORS, carried over from the servlet configuration with the same origins and methods.
 *
 * <p>Credentials are now allowed because the Server-Sent Events stream authenticates from the
 * {@code access_token} cookie, and a browser will not attach a cookie to a cross-origin
 * {@code EventSource} unless the server permits credentials. That in turn rules out a wildcard
 * origin, which is why the list stays explicit.
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins(
            "http://localhost:3000",
            "https://localhost:3000",
            "http://boardroom.kemri.go.ke:7102",
            "https://boardroom.kemri.go.ke:7102",
            "https://10.0.2.72",
            "http://10.0.2.72")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}
