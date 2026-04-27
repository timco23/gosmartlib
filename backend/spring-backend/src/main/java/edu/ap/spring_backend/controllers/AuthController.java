package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.LoginResponse;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST controller responsible for authentication-related endpoints.
 * Provides user info based on JWT, dev tokens, or Spring Security context,
 * and supports refreshing JWT tokens.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Returns the currently authenticated user.
     * Tries multiple strategies:
     * 1) Dev token (for development use)
     * 2) Spring Security authentication context
     * 3) JWT parsed from Authorization header
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

        // 1) DEV tokens
        Optional<User> devUser = findDevUserByToken(token);
        if (devUser.isPresent()) {
            return ResponseEntity.ok(toLoginResponse(devUser.get()));
        }

        // 2) SecurityContext
        Optional<User> authUser = findUserFromAuthentication(authentication);
        if (authUser.isPresent()) {
            return ResponseEntity.ok(toLoginResponse(authUser.get()));
        }

        // 3) Fallback: parse JWT direct from Authorization header
        Optional<User> jwtUser = findUserByJwtToken(token);
        if (jwtUser.isPresent()) {
            return ResponseEntity.ok(toLoginResponse(jwtUser.get()));
        }

        return ResponseEntity.status(401).build();
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
     * Maps a User entity to a LoginResponse DTO.
     */
    private LoginResponse toLoginResponse(User user) {
        return new LoginResponse(user.getName(), user.getRole().name(), user.getEmail());
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

    /**
     * Returns a development user based on predefined dev tokens.
     */
    private Optional<User> findDevUserByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String email;
        switch (token) {
            case "DEV_ADMIN_TOKEN":
                email = "admin@gsmartlib.tech";
                break;
            case "DEV_TEACHER_TOKEN":
                email = "leerkracht.dev@gsmartlib.tech";
                break;
            case "DEV_BIBLIOTHEEKBEHEERDER_TOKEN":
                email = "bibliotheekbeheerder.dev@gsmartlib.tech";
                break;
            case "DEV_STUDENT_TOKEN":
                email = "leerling.dev@gsmartlib.tech";
                break;
            default:
                return Optional.empty();
        }

        return userRepository.findByEmail(email);
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
}