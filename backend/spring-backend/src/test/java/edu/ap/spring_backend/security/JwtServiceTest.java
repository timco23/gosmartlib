package edu.ap.spring_backend.security;

import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @Test
    void generateToken_andParse_containsExpectedClaims() {
        JwtService jwtService = new JwtService("0123456789abcdef0123456789abcdef", 60);

        User user = mock(User.class);
        when(user.getId()).thenReturn(123);
        when(user.getRole()).thenReturn(Role.ADMIN);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getName()).thenReturn("Doe");
        when(user.getFirstName()).thenReturn("Jane");

        String token = jwtService.generateToken(user);

        assertNotNull(token);

        Jws<Claims> parsed = jwtService.parse(token);
        Claims claims = parsed.getPayload();

        assertEquals("123", claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));
        assertEquals("user@example.com", claims.get("email"));
        assertEquals("Doe", claims.get("name"));
        assertEquals("Jane", claims.get("firstName"));
        assertEquals(123, jwtService.getUserIdFromToken(token));
    }
}