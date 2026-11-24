package dev.johnmaluki.boardroom_booking_backend.core.service;

import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Access to the acting user.
 *
 * <p>Everything returns a {@code Mono} because under WebFlux the security context lives in the
 * Reactor context, which is only reachable from inside a reactive pipeline — there is no thread
 * local to read synchronously as there was with {@code SecurityContextHolder}.
 */
@Service
public class CurrentUserService {

  public Mono<UserPrincipal> getCurrentUser() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getPrincipal)
        .cast(UserPrincipal.class);
  }

  public Mono<Long> getUserId() {
    return getCurrentUser().map(UserPrincipal::getUserId);
  }

  public Mono<RoleType> getUserRole() {
    return getCurrentUser().map(UserPrincipal::getAuthority);
  }

  public Mono<AppUser> getAppUser() {
    return getCurrentUser().map(UserPrincipal::getUser);
  }

  public Mono<String> getAppUserTimezone() {
    return getCurrentUser().map(UserPrincipal::getUserTimeZone);
  }

  /**
   * The id a listing should be restricted to, or an empty {@code Optional} for an administrator who
   * should see everything. The repositories read a null id as "no restriction", so this is the one
   * place that decides between a scoped and an unscoped query.
   *
   * <p>An {@code Optional} rather than an empty {@code Mono} on purpose: an empty {@code Mono} would
   * short-circuit the surrounding pipeline and silently return nothing at all for administrators.
   */
  public Mono<Optional<Long>> getReadFilterUserId() {
    return getCurrentUser()
        .map(
            principal ->
                principal.isAdmin() ? Optional.<Long>empty() : Optional.of(principal.getUserId()));
  }
}
