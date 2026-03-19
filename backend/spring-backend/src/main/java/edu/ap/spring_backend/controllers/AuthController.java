package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import java.util.Optional;
import java.util.Map;

/**
 * REST controller for authentication operations.
 * Handles login requests and returns user information.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expires-minutes}")
    private long jwtExpiresMinutes;

    /**
     * Constructor injection for AuthService.
     */
    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the currently authenticated user based on the Authorization header.
     * Expected header: {@code Authorization: Bearer <jwt>}.
     * The token subject is used as the user id.
     *
     * @param authorizationHeader Authorization header (optional)
     * @return basic user info if token is valid, otherwise 401
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    try {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();

        JwtService jwt = new JwtService(jwtSecret, jwtExpiresMinutes);
        Integer userId = jwt.getUserIdFromToken(token);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        User u = userOpt.get();

        /// Makes a LoginResponse like the frontend expects (name/role/email)
        LoginResponse resp = new LoginResponse(u.getName(), u.getRole().name(), u.getEmail());
        return ResponseEntity.ok(resp);

    } catch (Exception e) {
        return ResponseEntity.status(401).build();
    }
    }

    /**
     * Refreshes an expired JWT using the refresh token.
     * @param refreshToken the refresh token stored in localStorage
     * @return new JWT or 401 if refresh token is invalid
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        // Find user by refresh token
        Optional<User> userOpt = userRepository.findByRefreshToken(refreshToken);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        User user = userOpt.get();
        // Generate new JWT
        JwtService jwtService = new JwtService(jwtSecret, jwtExpiresMinutes);
        String newJwt = jwtService.generateToken(user);

        return ResponseEntity.ok(Map.of("token", newJwt));
    }
}