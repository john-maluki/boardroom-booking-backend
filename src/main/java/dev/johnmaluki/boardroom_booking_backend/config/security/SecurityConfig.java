package dev.johnmaluki.boardroom_booking_backend.config.security;

import dev.johnmaluki.boardroom_booking_backend.config.security.filter.AppUserDetailsService;
import dev.johnmaluki.boardroom_booking_backend.config.security.filter.BoardroomUsernamePasswordAuthenticationFilter;
import dev.johnmaluki.boardroom_booking_backend.config.security.filter.JwtAuthenticationFilter;
import dev.johnmaluki.boardroom_booking_backend.config.security.filter.RefreshTokenAuthenticationFilter;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final LdapService ldapService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RefreshTokenAuthenticationFilter refreshTokenAuthenticationFilter;

    @DependsOn("authenticationManager")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        BoardroomUsernamePasswordAuthenticationFilter boardroomUsernamePasswordFilter = new BoardroomUsernamePasswordAuthenticationFilter(authenticationManager(), jwtService);
        boardroomUsernamePasswordFilter.setFilterProcessesUrl("/auth/login");
        boardroomUsernamePasswordFilter.setLdapService(ldapService);
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationManager(authenticationManager())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAt(boardroomUsernamePasswordFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, BoardroomUsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        .requestMatchers("/boardrooms/**")
                        .hasAnyAuthority("USER", "ADMIN")
                        .anyRequest().authenticated()

                );
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return this.authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public FilterRegistrationBean<RefreshTokenAuthenticationFilter> refreshTokenFilter () {
        FilterRegistrationBean<RefreshTokenAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(refreshTokenAuthenticationFilter);
        registrationBean.addUrlPatterns("/auth/refresh-token"); // Apply to the custom URL pattern
        return registrationBean;
    }
}
