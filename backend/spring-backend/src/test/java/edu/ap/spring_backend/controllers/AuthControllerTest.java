package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.LoginResponse;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AuthController(userRepository, jwtService);
    }

    private User mockUser(String name, String email, Role role) {
        User user = mock(User.class);
        when(user.getName()).thenReturn(name);
        when(user.getEmail()).thenReturn(email);
        when(user.getRole()).thenReturn(role);
        return user;
    }

    @Test
    void givenDevToken_whenMe_thenReturnsUser() {
        User user = mockUser("Admin", "admin@gsmartlib.tech", Role.ADMIN);
        when(userRepository.findByEmail("admin@gsmartlib.tech")).thenReturn(Optional.of(user));

        ResponseEntity<LoginResponse> response = controller.me(null, "Bearer DEV_ADMIN_TOKEN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Admin", response.getBody().getName());
        assertEquals("ADMIN", response.getBody().getRole());
        assertEquals("admin@gsmartlib.tech", response.getBody().getEmail());
    }

    @Test
    void givenAuthentication_whenMe_thenReturnsUser() {
        User user = mockUser("User", "user@example.com", Role.LEERLING);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("123");

        when(userRepository.findById(123)).thenReturn(Optional.of(user));

        ResponseEntity<LoginResponse> response = controller.me(auth, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User", response.getBody().getName());
    }

    @Test
    void givenJwtToken_whenMe_thenReturnsUser() {
        User user = mockUser("User", "user@example.com", Role.LEERLING);

        when(jwtService.getUserIdFromToken("jwt")).thenReturn(123);
        when(userRepository.findById(123)).thenReturn(Optional.of(user));

        ResponseEntity<LoginResponse> response = controller.me(null, "Bearer jwt");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user@example.com", response.getBody().getEmail());
    }

    @Test
    void givenNoAuth_whenMe_thenUnauthorized() {
        ResponseEntity<LoginResponse> response = controller.me(null, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void givenValidRefreshToken_whenRefresh_thenReturnsNewToken() {
        User user = mockUser("User", "user@example.com", Role.LEERLING);

        when(userRepository.findByRefreshToken("rt")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("newJwt");

        ResponseEntity<Map<String, String>> response = controller.refresh(Map.of("refreshToken", "rt"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("newJwt", response.getBody().get("token"));
    }

    @Test
    void givenMissingRefreshToken_whenRefresh_thenUnauthorized() {
        ResponseEntity<Map<String, String>> response = controller.refresh(Map.of());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void givenNullBody_whenRefresh_thenUnauthorized() {
        ResponseEntity<Map<String, String>> response = controller.refresh(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}