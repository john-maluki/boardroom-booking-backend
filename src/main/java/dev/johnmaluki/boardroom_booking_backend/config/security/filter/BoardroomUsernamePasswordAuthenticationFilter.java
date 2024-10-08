package dev.johnmaluki.boardroom_booking_backend.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.johnmaluki.boardroom_booking_backend.config.security.JwtService;
import dev.johnmaluki.boardroom_booking_backend.config.security.LdapService;
import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import dev.johnmaluki.boardroom_booking_backend.core.service.ProfileChecker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BoardroomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Setter
    private LdapService ldapService;
    @Setter
    private ProfileChecker profileChecker;

    public BoardroomUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        String username;
        String password;

        try {
            Map<String, String> requestMap = new ObjectMapper().readValue(request.getInputStream(), Map.class);
            username = requestMap.get("username");
            password = requestMap.get("password");
        } catch (RuntimeException | IOException e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }

        if (!profileChecker.checkIfAppRunningLocally()) {
            UserPrincipal userPrincipal = ldapService.authenticateAndRetrieveUserInfo(username, password); // authenticate from AD
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    null,
                    userPrincipal.getAuthorities()
            );
            // Optionally, set this authentication object in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication;
        } else {
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    username, password, Collections.emptyList());
            return authenticationManager.authenticate(authRequest);
        }

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write("{\"status\":\"error\",\"message\":\"Authentication failed: " + failed.getMessage() + "\"}");
        writer.flush();
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        UserPrincipal userPrincipal = (UserPrincipal) authResult.getPrincipal();
        Map<String, Object> claim = new HashMap<>();
        claim.put("role", userPrincipal.getUser().getRole().getAuthority());
        claim.put("fullName", userPrincipal.getUser().getFullName());
        claim.put("email", userPrincipal.getUser().getEmail());
        claim.put("timezone", userPrincipal.getUser().getTimeZone());
        claim.put("id", userPrincipal.getUser().getId());
        String accessToken = jwtService.generateToken(userPrincipal.getUsername(), claim);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal.getUsername());

        Cookie cookie = new Cookie("access_token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(5000);

        response.addCookie(cookie);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }
}
