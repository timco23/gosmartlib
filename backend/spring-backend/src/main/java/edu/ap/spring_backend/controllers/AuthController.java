package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.LoginRequest;
import edu.ap.spring_backend.dto.LoginResponse;
import edu.ap.spring_backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;

/**
 * REST controller for authentication operations.
 * Handles login requests and returns user information.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    
    /**
     * Constructor injection for AuthService.
     * @param authService Service handling authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Authenticates a user with email and password.
     * @param request LoginRequest containing email and password
     * @return ResponseEntity with LoginResponse containing user name and role, or 401 if authentication fails
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            logger.info("Login attempt for email: {}", request.getEmail());
            LoginResponse response = authService.login(request);
            logger.info("Login successful for: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Login failed for email: {}. Error: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(401).build();
        }
    }
    
}