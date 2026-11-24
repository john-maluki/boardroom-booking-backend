package dev.johnmaluki.boardroom_booking_backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The servlet-era {@code DaoAuthenticationProvider} wiring is gone. Password checking against the
 * embedded dev directory now happens in {@code EmbeddedLdapService}, which uses the {@link
 * PasswordEncoder} bean below directly, so there is no authentication provider to configure.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppConfig {

  @Profile("dev")
  @Bean
  public Faker faker() {
    return new Faker();
  }

  /**
   * {@code ignoreIfMissing} so the application can also be configured purely from real environment
   * variables, which is how it runs in a container. {@code Dotenv.load()} threw when the file was
   * absent, meaning no {@code .env} on disk took the whole context down before any bean that
   * actually needed a secret was reached.
   */
  @Bean
  public Dotenv dotenv() {
    return Dotenv.configure().ignoreIfMissing().load();
  }

  @Profile("dev")
  @Bean
  public LdapContextSource contextSourceDev() {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl("ldap://localhost:9000");
    contextSource.setBase("dc=springframework,dc=org");
    contextSource.afterPropertiesSet();
    return contextSource;
  }

  @Profile("dev")
  @Bean
  public LdapTemplate ldapTemplateDev() {
    return new LdapTemplate(contextSourceDev());
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Profile("dev")
  @Bean
  @DependsOn("dotenv")
  public JavaMailSender javaMailSenderDev() {
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
    javaMailSender.setHost(dotenv().get("DEV_MAIL_HOST"));
    javaMailSender.setUsername(dotenv().get("DEV_MAIL_USERNAME"));
    javaMailSender.setPassword(dotenv().get("DEV_MAIL_PASSWORD"));
    javaMailSender.setPort(Integer.parseInt(dotenv().get("DEV_MAIL_PORT")));

    Properties props = javaMailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    return javaMailSender;
  }

  @Profile("prod")
  @Bean
  @DependsOn("dotenv")
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
    javaMailSender.setHost(dotenv().get("PROD_MAIL_HOST"));
    javaMailSender.setUsername(dotenv().get("PROD_MAIL_USERNAME"));
    javaMailSender.setPassword(dotenv().get("PROD_MAIL_PASSWORD"));
    javaMailSender.setPort(Integer.parseInt(dotenv().get("PROD_MAIL_PORT")));

    Properties props = javaMailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.starttls.required", "true");
    return javaMailSender;
  }

  @Bean
  @DependsOn("dotenv")
  public CredentialsProvider credentialsProvider() {
    String username = dotenv().get("DEV_NAV_USERNAME");
    String password = dotenv().get("DEV_NAV_PASSWORD");
    // Parsed leniently: an unset or malformed port should not stop the whole context from
    // starting, since only the employee-directory lookup depends on it.
    int port = parsePort(dotenv().get("DEV_NAV_PORT"));
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(
        new AuthScope(AuthScope.ANY_HOST, port),
        new NTCredentials(username, password, null, null));
    return credentialsProvider;
  }

  private int parsePort(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException | NullPointerException e) {
      log.warn("DEV_NAV_PORT is not a usable port ({}); matching credentials on any port", value);
      return AuthScope.ANY_PORT;
    }
  }
}
