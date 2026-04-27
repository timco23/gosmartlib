package edu.ap.spring_backend.security;

import edu.ap.spring_backend.jpa.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for creating and validating JSON Web Tokens (JWT).
 * Handles token generation, parsing, and extracting user information.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expiresMinutes;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expires-minutes}") long expiresMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiresMinutes = expiresMinutes;
    }

      /**
     * Creates a signed JWT for the given user.
     * @param user the user to include in the token
     * @return signed JWT string
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresMinutes * 60);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("firstName", user.getFirstName());

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

      /**
     * Verifies the token signature and returns its claims.
     * @param token raw JWT (without "Bearer ")
     * @return parsed JWT claims
     */
    public Jws<Claims> parse(String token) {
    return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token);
    }

     /**
     * Extracts the user ID from the JWT subject (sub claim).
     * @param token raw JWT
     * @return user ID
     */
    public Integer getUserIdFromToken(String token) {
        String sub = parse(token).getPayload().getSubject();
        return Integer.valueOf(sub);
    }
}