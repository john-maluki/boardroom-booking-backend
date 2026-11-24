package dev.johnmaluki.boardroom_booking_backend.config.security.impl;

import dev.johnmaluki.boardroom_booking_backend.config.security.LdapService;
import dev.johnmaluki.boardroom_booking_backend.config.security.LdapUserDetail;
import java.util.List;
import javax.naming.directory.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Local development directory, backed by the embedded LDAP server seeded from {@code
 * test-server.ldif}.
 *
 * <p>The fixture stores bcrypt hashes in {@code userPassword}, so the password is checked here with
 * the application's {@link PasswordEncoder}. Previously this comparison was done implicitly by a
 * {@code DaoAuthenticationProvider}; doing it explicitly removes the servlet-era authentication
 * provider plumbing without changing which credentials are accepted.
 *
 * <p>Spring LDAP is blocking, so the lookup runs on {@code boundedElastic} for the same reason the
 * Active Directory path does.
 */
@Profile("dev")
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddedLdapService implements LdapService {
  private final LdapTemplate ldapTemplate;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Mono<LdapUserDetail> authenticate(String username, String password) {
    return Mono.fromCallable(() -> findStoredPassword(username))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            storedPassword -> {
              if (!passwordEncoder.matches(password, storedPassword)) {
                log.info("Embedded directory rejected the password for {}", username);
                return Mono.empty();
              }
              return Mono.just(
                  LdapUserDetail.builder()
                      .username(username)
                      .name(username)
                      .email(username + "@test.com")
                      .build());
            });
  }

  private String findStoredPassword(String username) {
    EqualsFilter filter = new EqualsFilter("uid", username);
    List<String> passwords =
        ldapTemplate.search("", filter.encode(), new UserPasswordAttributesMapper());
    if (passwords.isEmpty()) {
      log.info("Embedded directory has no entry for {}", username);
      return null;
    }
    return passwords.get(0);
  }

  private static class UserPasswordAttributesMapper implements AttributesMapper<String> {
    @Override
    public String mapFromAttributes(Attributes attrs) throws javax.naming.NamingException {
      Object stored = attrs.get("userPassword").get();
      return stored instanceof byte[] bytes ? new String(bytes) : String.valueOf(stored);
    }
  }
}
