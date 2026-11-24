package dev.johnmaluki.boardroom_booking_backend.config.security;

import dev.johnmaluki.boardroom_booking_backend.core.exception.JwtTokenException;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Issues and verifies the application's JWTs.
 *
 * <p>Written against the jjwt 0.12+ builder API ({@code claims().add(..)}, {@code verifyWith(..)},
 * {@code parseSignedClaims(..)}); the pre-0.12 {@code setClaims}/{@code parserBuilder} calls this
 * replaced were removed upstream.
 *
 * <p>Note on ordering: the subject is set after the extra claims so a stray {@code sub} in the
 * claim map can never displace the real username.
 */
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

  private String buildToken(
      String username, Map<String, Object> extraClaims, long expirationTime) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .claims(extraClaims)
        .subject(username)
        .issuedAt(new Date(now))
        .expiration(new Date(now + expirationTime))
        .signWith(this.getSigningKey(), Jwts.SIG.HS256)
        .compact();
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

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    Claims claims = extractClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (ExpiredJwtException
        | UnsupportedJwtException
        | MalformedJwtException
        | SignatureException
        | IllegalArgumentException e) {
      throw new JwtTokenException(String.format("Token issue %s: ", e.getMessage()));
    }
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64URL.decode(this.getJwtSecretKey());
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private String getJwtSecretKey() {
    String secret = dotenv.get("JWT_SECRET_KEY");
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT_SECRET_KEY is not set");
    }
    return secret;
  }
}
