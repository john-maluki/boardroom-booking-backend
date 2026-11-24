package dev.johnmaluki.boardroom_booking_backend.config.security.impl;

import dev.johnmaluki.boardroom_booking_backend.config.security.LdapService;
import dev.johnmaluki.boardroom_booking_backend.config.security.LdapUserDetail;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Binds against the KEMRI Active Directory to authenticate, then reads the user's display name and
 * principal name.
 *
 * <p>JNDI is a blocking API with no reactive counterpart, so the whole exchange is wrapped in
 * {@code Mono.fromCallable} and pushed onto {@code boundedElastic}. That scheduler exists for
 * precisely this case: it grows to absorb blocking calls and keeps them off the small, fixed pool
 * of Netty event loops, where a directory that is slow to answer would otherwise stall every other
 * in-flight request on that loop.
 */
@Profile("!dev")
@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveDirectoryLdapService implements LdapService {
  private static final String BASE_SEARCH_DN = "dc=kemri,dc=org";

  private final Dotenv dotenv;

  @Override
  public Mono<LdapUserDetail> authenticate(String username, String password) {
    return Mono.fromCallable(() -> bindAndRead(username, password))
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(
            NamingException.class,
            error -> {
              log.info("Directory rejected user {}: {}", username, error.getMessage());
              return Mono.empty();
            });
  }

  private LdapUserDetail bindAndRead(String username, String password) throws NamingException {
    DirContext context = authenticateUserFromActiveDirectory(username, password);
    try {
      return retrieveUserInformation(context, username);
    } finally {
      try {
        context.close();
      } catch (NamingException e) {
        log.debug("Could not close directory context: {}", e.getMessage());
      }
    }
  }

  private DirContext authenticateUserFromActiveDirectory(String username, String password)
      throws NamingException {
    Hashtable<String, String> env = new Hashtable<>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, dotenv.get("LDAP_PROD_SERVER_URL_1"));
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, username + "@" + dotenv.get("LDAP_PROD_DOMAIN_NAME"));
    env.put(Context.SECURITY_CREDENTIALS, password);
    // Creating the context is the bind, and therefore the password check.
    return new InitialDirContext(env);
  }

  private LdapUserDetail retrieveUserInformation(DirContext context, String username)
      throws NamingException {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    String searchFilter = "(sAMAccountName=" + username + ")";
    NamingEnumeration<SearchResult> results =
        context.search(BASE_SEARCH_DN, searchFilter, searchControls);
    if (!results.hasMore()) {
      throw new NamingException("No directory entry for " + username);
    }
    Attributes attributes = results.next().getAttributes();
    return LdapUserDetail.builder()
        .username(username)
        .name(attributeValue(attributes, "displayName", username))
        .email(attributeValue(attributes, "userPrincipalName", null))
        .build();
  }

  private String attributeValue(Attributes attributes, String name, String fallback)
      throws NamingException {
    Attribute attribute = attributes.get(name);
    return attribute == null ? fallback : (String) attribute.get();
  }
}
