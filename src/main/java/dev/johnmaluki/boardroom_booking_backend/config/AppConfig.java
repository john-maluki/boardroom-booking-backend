package dev.johnmaluki.boardroom_booking_backend.config;

import dev.johnmaluki.boardroom_booking_backend.config.security.AppUserDetailsService;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    @Profile("dev")
    @Bean
    public Faker faker() {
        return new Faker();
    }
    @Bean
    public Dotenv dotenv() {
        return Dotenv.load();
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
    public AuthenticationProvider authenticationProviderDev(AppUserDetailsService userPrincipalDetailService){
        userPrincipalDetailService.setLdapTemplate(ldapTemplateDev());
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userPrincipalDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    @DependsOn("dotenv")
    public JavaMailSender javaMailSender() {
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

    @Bean
    @DependsOn("dotenv")
    public CredentialsProvider credentialsProvider() {
        String username = dotenv().get("DEV_NAV_USERNAME");
        String password = dotenv().get("DEV_NAV_PASSWORD");
        int port = Integer.parseInt(dotenv().get("DEV_NAV_PORT"));
        // Create credentials provider with NTLM credentials
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, port),
                new NTCredentials(username, password, null, null)
        );
        return credentialsProvider;
    }

}
