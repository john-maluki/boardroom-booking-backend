package dev.johnmaluki.boardroom_booking_backend.config;

import dev.johnmaluki.boardroom_booking_backend.config.security.filter.AppUserDetailsService;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private final AppUserDetailsService userPrincipalDetailService;

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

    @Profile("prod")
    @DependsOn("dotenv")
    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(dotenv().get("LDAP_PROD_SERVER_URL_1"));
        contextSource.setBase("dc=kemri,dc=org");
        contextSource.setUserDn(dotenv().get("LDAP_PROD_SERVER_USERNAME"));
        contextSource.setPassword(dotenv().get("LDAP_PROD_SERVER_PASSWORD"));
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Profile("dev")
    @Bean
    public LdapTemplate ldapTemplateDev() {
        return new LdapTemplate(contextSourceDev());
    }

    @Profile("prod")
    @Bean
    public LdapTemplate ldapTemplate() {
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource());
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Profile("prod")
    @Bean
    public AuthenticationProvider authenticationProvider(){
        userPrincipalDetailService.setLdapTemplate(ldapTemplate());
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userPrincipalDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Profile("dev")
    @Bean
    public AuthenticationProvider authenticationProviderDev(){
        userPrincipalDetailService.setLdapTemplate(ldapTemplateDev());
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userPrincipalDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

}
