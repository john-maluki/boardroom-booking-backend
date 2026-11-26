package dev.johnmaluki.boardroom_booking_backend.config.security.impl;

import static org.assertj.core.api.Assertions.assertThat;

import dev.johnmaluki.boardroom_booking_backend.config.security.LdapService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Pins down which {@link LdapService} wins for every profile / {@code
 * app.auth.directory-provider} combination {@code EmbeddedLdapService} and {@code
 * ActiveDirectoryLdapService}'s annotations are meant to produce. A compile cannot catch a wrong
 * {@code havingValue} string or a missing {@code matchIfMissing}, and getting this wrong either
 * leaves the app with no {@code LdapService} bean at all (fails fast, at least) or — worse —
 * silently authenticates a non-dev run against the local test directory.
 *
 * <p>Runs real {@code @Profile}/{@code @ConditionalOnProperty} evaluation through {@link
 * ApplicationContextRunner} rather than reading the annotations by reflection, so it exercises the
 * exact mechanism Spring itself uses at startup. {@code spring.profiles.active} is set as a plain
 * property rather than through a dedicated API because there isn't one here: {@code
 * AbstractEnvironment.getActiveProfiles()} falls back to reading that property itself when no
 * profile has been set explicitly, which is what activates {@code @Profile("dev")} beans below.
 * Both real implementations' other dependencies ({@code LdapTemplate}, {@code Dotenv}, {@code
 * PasswordEncoder}) are stubbed out, since this test cares only about which class gets
 * registered, not what either does once selected.
 */
class LdapServiceSelectionTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(
              StubDependencies.class, EmbeddedLdapService.class, ActiveDirectoryLdapService.class);

  @Test
  void devWithNoPropertySetUsesTheEmbeddedDirectory() {
    contextRunner
        .withPropertyValues("spring.profiles.active=dev")
        .run(
            context ->
                assertThat(context)
                    .hasSingleBean(LdapService.class)
                    .getBean(LdapService.class)
                    .isInstanceOf(EmbeddedLdapService.class));
  }

  @Test
  void devWithEmbeddedExplicitlySetUsesTheEmbeddedDirectory() {
    contextRunner
        .withPropertyValues("spring.profiles.active=dev", "app.auth.directory-provider=embedded")
        .run(
            context ->
                assertThat(context)
                    .getBean(LdapService.class)
                    .isInstanceOf(EmbeddedLdapService.class));
  }

  @Test
  void devWithActiveDirectorySetUsesActiveDirectoryInstead() {
    contextRunner
        .withPropertyValues(
            "spring.profiles.active=dev", "app.auth.directory-provider=active-directory")
        .run(
            context ->
                assertThat(context)
                    .hasSingleBean(LdapService.class)
                    .getBean(LdapService.class)
                    .isInstanceOf(ActiveDirectoryLdapService.class));
  }

  @Test
  void prodAlwaysUsesActiveDirectory() {
    contextRunner
        .withPropertyValues(
            "spring.profiles.active=prod", "app.auth.directory-provider=active-directory")
        .run(
            context ->
                assertThat(context)
                    .hasSingleBean(LdapService.class)
                    .getBean(LdapService.class)
                    .isInstanceOf(ActiveDirectoryLdapService.class));
  }

  @Test
  void prodNeverRegistersTheEmbeddedDirectoryEvenIfTheirPropertyIsMisconfigured() {
    // A real prod run would never set this; the point is that @Profile("dev") on
    // EmbeddedLdapService is what protects prod, not the property having the right value.
    contextRunner
        .withPropertyValues("spring.profiles.active=prod", "app.auth.directory-provider=embedded")
        .run(context -> assertThat(context).doesNotHaveBean(EmbeddedLdapService.class));
  }

  /** Replaces the collaborators the two real implementations would otherwise need wired in. */
  @Configuration
  static class StubDependencies {
    @Bean
    LdapTemplate ldapTemplate() {
      // A real ContextSource would try to reach an actual directory; LdapTemplate validates
      // it's non-null in afterPropertiesSet() but EmbeddedLdapService never calls through to
      // it here, so a mock satisfies construction without needing anything real behind it.
      return new LdapTemplate(org.mockito.Mockito.mock(ContextSource.class));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
      // PasswordEncoder isn't a functional interface (encode() has no default), and this bean
      // is never actually invoked here — EmbeddedLdapService just needs one to construct.
      return new PasswordEncoder() {
        @Override
        public String encode(CharSequence rawPassword) {
          return String.valueOf(rawPassword);
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
          return false;
        }
      };
    }

    @Bean
    io.github.cdimascio.dotenv.Dotenv dotenv() {
      return io.github.cdimascio.dotenv.Dotenv.configure().ignoreIfMissing().load();
    }
  }
}
