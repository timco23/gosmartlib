package edu.ap.spring_backend.security;

import edu.ap.spring_backend.jpa.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.Map;

/**
 * Utility class for creating and validating JWT tokens.
 */
public class JwtService {

    private final String secret;
    private final long expiresMinutes;

    /**
     * @param secret secret used to sign/verify tokens
     * @param expiresMinutes token lifetime in minutes
     */
    public JwtService(String secret, long expiresMinutes) {
        this.secret = secret;
        this.expiresMinutes = expiresMinutes;
    }

   /**
     * Creates a signed JWT for a user.
     * @param user user to include in the token
     * @return JWT string
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
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

     /**
     * Verifies the token signature and returns its claims.
     * @param token raw JWT (without "Bearer ")
     * @return parsed token payload and claims
     */
    public Jws<Claims> parse(String token) {
    return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token);
    }

    /**
     * @param token raw JWT
     * @return user id from the token subject (sub)
     */
    public Integer getUserIdFromToken(String token) {
        String sub = parse(token).getPayload().getSubject();
        return Integer.valueOf(sub);
    }
}