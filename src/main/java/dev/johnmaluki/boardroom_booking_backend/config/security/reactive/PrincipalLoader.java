package dev.johnmaluki.boardroom_booking_backend.config.security.reactive;

import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.RoleRepository;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Builds a {@link UserPrincipal} from the database.
 *
 * <p>The role is a separate row under R2DBC, so it is fetched alongside the user and folded into
 * the principal here. Centralising that means token authentication, login and the dev LDAP path all
 * produce a principal with its authority already resolved, and nothing downstream has to go back to
 * the database to find out whether the caller is an administrator.
 */
@Component
@RequiredArgsConstructor
public class PrincipalLoader {
  private final AppUserRepository userRepository;
  private final RoleRepository roleRepository;

  public Mono<UserPrincipal> loadByUsername(String username) {
    return userRepository.findByUsername(username).flatMap(this::withRole);
  }

  public Mono<UserPrincipal> withRole(AppUser user) {
    return roleRepository
        .findByUserId(user.getId())
        .map(role -> new UserPrincipal(user, role.getAuthority()))
        .defaultIfEmpty(new UserPrincipal(user, RoleType.USER));
  }
}
