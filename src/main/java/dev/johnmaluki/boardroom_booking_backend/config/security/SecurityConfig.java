package dev.johnmaluki.boardroom_booking_backend.config.security;

import dev.johnmaluki.boardroom_booking_backend.config.security.reactive.BearerTokenAuthenticationConverter;
import dev.johnmaluki.boardroom_booking_backend.config.security.reactive.JwtAuthenticationManager;
import dev.johnmaluki.boardroom_booking_backend.config.security.reactive.RestAccessDeniedHandler;
import dev.johnmaluki.boardroom_booking_backend.config.security.reactive.RestAuthenticationEntryPoint;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Reactive security.
 *
 * <p>The servlet chain this replaces consisted of three filters plus a {@code
 * FilterRegistrationBean}. Here authentication is a single {@link AuthenticationWebFilter}, and
 * login and refresh have become ordinary endpoints rather than filters, so the chain only has to
 * deal with verifying tokens.
 *
 * <p>The API is stateless: {@link NoOpServerSecurityContextRepository} stops Spring from trying to
 * stash the security context in a session between requests, which is what {@code
 * SessionCreationPolicy.STATELESS} did before.
 *
 * <p>Path rules mirror the previous configuration, including letting anything not named fall
 * through to "must be authenticated".
 *
 * <p>{@code exceptionHandling} is what gives an unauthenticated or under-privileged request the
 * same {@code ResourceErrorMessage}-shaped body every other error in the API returns — a failure
 * here is caught by {@code ExceptionTranslationWebFilter} inside the security chain itself, never
 * by {@code @ExceptionHandler}, so without wiring these two in it would fall back to Spring
 * Security's own bare, empty-bodied defaults instead.
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthenticationManager jwtAuthenticationManager;
  private final BearerTokenAuthenticationConverter bearerTokenAuthenticationConverter;
  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
  private final RestAccessDeniedHandler restAccessDeniedHandler;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
    jwtFilter.setServerAuthenticationConverter(bearerTokenAuthenticationConverter);
    jwtFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .logout(ServerHttpSecurity.LogoutSpec::disable)
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        // Without an explicit entry point, WebFlux falls back to the HTTP Basic one and sends
        // "WWW-Authenticate: Basic", which makes browsers pop up their native sign-in dialog
        // when the frontend calls the API on the same origin — RestAuthenticationEntryPoint
        // avoids that same way HttpStatusServerEntryPoint(UNAUTHORIZED) did, and additionally
        // gives the 401 (and the 403 below) a real JSON body instead of an empty one.
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler))
        .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .authorizeExchange(
            exchange ->
                exchange
                    .pathMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .pathMatchers("/")
                    .permitAll()
                    .pathMatchers(HttpMethod.POST, "/auth/**")
                    .permitAll()
                    .pathMatchers(
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/webjars/**")
                    .permitAll()
                    .pathMatchers("/boardrooms/**", "/locked-boardrooms/**")
                    .hasAnyAuthority(RoleType.USER.name(), RoleType.ADMIN.name())
                    .pathMatchers("/reservations/**")
                    .hasAnyAuthority(RoleType.USER.name(), RoleType.ADMIN.name())
                    .pathMatchers("/equipments/**")
                    .hasAnyAuthority(RoleType.USER.name(), RoleType.ADMIN.name())
                    .pathMatchers("/users/**")
                    .hasAnyAuthority(RoleType.USER.name(), RoleType.ADMIN.name())
                    .pathMatchers("/files/**", "/upload/**")
                    .hasAnyAuthority(RoleType.USER.name(), RoleType.ADMIN.name())
                    .pathMatchers("/notifications/**")
                    .hasAnyAuthority(RoleType.USER.name(), RoleType.ADMIN.name())
                    .anyExchange()
                    .authenticated())
        .build();
  }
}
