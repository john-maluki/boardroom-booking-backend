package dev.johnmaluki.boardroom_booking_backend.config.security;

import dev.johnmaluki.boardroom_booking_backend.core.exception.JwtTokenException;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final Dotenv dotenv;

    @Value("${application.security.jwt.expiration}")
    private long tokenExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;


    public String generateToken(String username, Map<String, Object> extraClaims) {
        return this.buildToken(username, extraClaims, this.tokenExpiration);
    }

    public String generateRefreshToken(String username) {
        return this.buildToken(username, new HashMap<>(), this.refreshTokenExpiration);
    }

    private String buildToken(String username, Map<String, Object> extraClaims, long expirationTime) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(this.getSignIngKey(), SignatureAlgorithm.HS256).compact();
    }

    public boolean isTokenValid(String token, String username) {
        String extractedUsername = this.extractUsername(token);
        return extractedUsername.equals(username) && !this.isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    private Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) throws JwtTokenException{
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws JwtTokenException{
        Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignIngKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException |
                 IllegalArgumentException e) {
            throw new JwtTokenException(String.format("Token issue %s: ", e.getMessage()));
        }
    }

    private Key getSignIngKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(this.getJwtSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getJwtSecretKey() {
        return dotenv.get("JWT_SECRET_KEY");
    }
}
