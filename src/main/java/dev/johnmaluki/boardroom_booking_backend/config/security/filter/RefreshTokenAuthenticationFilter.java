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
            final String refreshToken;
            final String username;
            try {
                Map<String, String> requestMap = new ObjectMapper().readValue(request.getInputStream(), Map.class);
                refreshToken = requestMap.get("refreshToken");
            } catch (IOException e) {
                return;
            }
            username = jwtService.extractUsername(refreshToken);
            if (username != null) {
                AppUser appUser = userRepository.findByUsername(username).orElseThrow();
                boolean tokenIsValid = jwtService.isTokenValid(refreshToken, username);
                if (tokenIsValid) {
                    Map<String, Object> claim = new HashMap<>();
                    claim.put("role", appUser.getRole().getAuthority());
                    claim.put("email", appUser.getEmail());
                    claim.put("fullName", appUser.getFullName());
                    claim.put("timezone", appUser.getTimeZone());
                    claim.put("id", appUser.getId());
                    String accessToken = jwtService.generateToken(username, claim);
                    Map<String, String> tokens = new HashMap<>();
                    tokens.put("accessToken", accessToken);
                    tokens.put("refreshToken", refreshToken);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), tokens);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
