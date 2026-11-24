package dev.johnmaluki.boardroom_booking_backend.config.security;

import reactor.core.publisher.Mono;

/**
 * Authenticates a username and password against a directory and returns what the directory knows
 * about them.
 *
 * <p>Deliberately no database access: provisioning the local {@code AppUser} row is the same work
 * regardless of which directory answered, so it lives once in {@code AuthService} instead of being
 * duplicated per implementation as it was before.
 *
 * <p>Implementations complete empty when the credentials are rejected.
 */
public interface LdapService {
  Mono<LdapUserDetail> authenticate(String username, String password);
}
