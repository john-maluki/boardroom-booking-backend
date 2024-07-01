package dev.johnmaluki.boardroom_booking_backend.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.johnmaluki.boardroom_booking_backend.config.security.JwtService;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RefreshTokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AppUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Get the request URI
        String requestURI = request.getRequestURI();

        // Check the request URL
        if (requestURI.startsWith("/auth/refresh-token")) {
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            final String refreshToken;
            final String username;
            if (authHeader == null && !authHeader.startsWith("Bearer ")) {
                return;
            }
            refreshToken = authHeader.substring(7);
            username = jwtService.extractUsername(refreshToken);
            if (username != null) {
                AppUser appUser = userRepository.findByUsername(username).orElseThrow();
                boolean tokenIsValid = jwtService.isTokenValid(refreshToken, username);
                if (tokenIsValid) {
                    Map<String, Object> claim = new HashMap<>();
                    claim.put("role", appUser.getRole().getAuthority());
                    String accessToken = jwtService.generateToken(username, claim);
                    Map<String, String> tokens = new HashMap<>();
                    tokens.put("access_token", accessToken);
                    tokens.put("refresh_token", refreshToken);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), tokens);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
