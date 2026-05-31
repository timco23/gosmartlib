package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.LoginResponse;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller responsible for authentication-related endpoints.
 * Provides user info based on JWT or Spring Security context,
 * and supports refreshing JWT tokens.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, SchoolRepository schoolRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Returns the currently authenticated user.
     * Tries multiple strategies:
     * 1) Spring Security authentication context
     * 2) JWT parsed from Authorization header
     * @param authentication Spring Security authentication object
     * @param authorizationHeader Authorization header containing Bearer token
     * @return LoginResponse with user info or 401 if not authenticated
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String token = extractBearerToken(authorizationHeader);

        // 1) SecurityContext
        Optional<User> authUser = findUserFromAuthentication(authentication);
        if (authUser.isPresent()) {
            return ResponseEntity.ok(toLoginResponse(authUser.get()));
        }

        // 2) Fallback: parse JWT direct from Authorization header
        Optional<User> jwtUser = findUserByJwtToken(token);
        if (jwtUser.isPresent()) {
            return ResponseEntity.ok(toLoginResponse(jwtUser.get()));
        }

        return ResponseEntity.status(401).build();
    }

    /**
     * Returns whether the initial setup still needs to be completed.
     * Setup is required when no school exists in the system yet.
     */
    @GetMapping("/setup-required")
    public ResponseEntity<Boolean> setupRequired() {
        return ResponseEntity.ok(schoolRepository.count() == 0);
    }

    /**
     * Email/password login for initial setup admin user.
     * Disabled once a school exists in the system (setup complete).
     * Returns a JWT and refresh token on valid credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        User user = userOpt.get();
        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).build();
        }

        String token = jwtService.generateToken(user);
        String refreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("token", token, "refreshToken", refreshToken));
    }

    /**
     * Changes the password of the currently authenticated user.
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> userOpt = findUserFromAuthentication(authentication);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        User user = userOpt.get();
        if (user.getPassword() == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(400).build();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setNeedsPasswordChange(false);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    /**
     * Generates a new JWT using a valid refresh token.
     * @param body request body containing the refresh token
     * @return new JWT token or 401 if invalid
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        if (body == null) {
            return ResponseEntity.status(401).build();
        }

        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findByRefreshToken(refreshToken);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        String newJwt = jwtService.generateToken(userOpt.get());
        return ResponseEntity.ok(Map.of("token", newJwt));
    }

    /**
     * Maps a User entity to a LoginResponse DTO.
     */
    private LoginResponse toLoginResponse(User user) {
        Long schoolId = user.getSchool() != null ? user.getSchool().getId() : null;
        return new LoginResponse(user.getName(), user.getFirstName(), user.getRole().name(), user.getEmail(), schoolId, user.isNeedsPasswordChange());
    }

    /**
     * Extracts the user from the Spring Security Authentication object.
     */
    private Optional<User> findUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String principal = authentication.getName();
        if (principal == null || principal.isBlank()) {
            return Optional.empty();
        }

        try {
            Integer userId = Integer.valueOf(principal);
            return userRepository.findById(userId);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves a user based on a JWT token.
     */
    private Optional<User> findUserByJwtToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            Integer userId = jwtService.getUserIdFromToken(token);
            return userRepository.findById(userId);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * Extracts the Bearer token from the Authorization header.
     */
    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }
}
