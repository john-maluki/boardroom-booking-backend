package dev.johnmaluki.boardroom_booking_backend.core.security;

import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

/**
 * Supplies {@code created_by} / {@code updated_by}.
 *
 * <p>Under WebFlux the authenticated principal travels in the Reactor context rather than a thread
 * local, so the auditor is read from {@link ReactiveSecurityContextHolder}. Work with no principal
 * attached — a Rabbit listener persisting a notification, or the dev data seeder — yields an empty
 * {@code Mono} and the audit columns stay null, which is what the JPA version did for
 * unauthenticated writes.
 */
public class ReactiveAuditorAwareImpl implements ReactiveAuditorAware<Long> {

  @Override
  public Mono<Long> getCurrentAuditor() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(Authentication::isAuthenticated)
        .map(Authentication::getPrincipal)
        .filter(UserPrincipal.class::isInstance)
        .cast(UserPrincipal.class)
        .map(UserPrincipal::getUserId);
  }
}
