package dev.johnmaluki.boardroom_booking_backend.config.security.impl;

import dev.johnmaluki.boardroom_booking_backend.config.security.LdapService;
import dev.johnmaluki.boardroom_booking_backend.config.security.LdapUserDetail;
import java.util.List;
import javax.naming.directory.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Local test directory, backed by the embedded LDAP server seeded from {@code test-server.ldif}.
 *
 * <p>This is the default in the {@code dev} profile, and the {@code dev} profile only — never in
 * {@code prod}, regardless of {@code app.auth.directory-provider}, so there is no way to end up
 * authenticating a real deployment against this fixture. Within {@code dev} it is one of two
 * interchangeable choices, selected by {@code app.auth.directory-provider} in {@code
 * application-dev.properties}:
 *
 * <ul>
 *   <li>{@code embedded} (default, or the property left unset) — this class, test users only.
 *   <li>{@code active-directory} — {@link ActiveDirectoryLdapService} instead, so a {@code dev}
 *       run can log in against the real KEMRI AD to test with real accounts. Everything else
 *       that is {@code dev}-profile-specific (sample data seeding, dev mail settings, the
 *       embedded LDAP server itself still running in the background) stays as it is; only which
 *       directory {@code AuthService} binds against changes.
 * </ul>
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
@ConditionalOnProperty(
    name = "app.auth.directory-provider",
    havingValue = "embedded",
    matchIfMissing = true)
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
