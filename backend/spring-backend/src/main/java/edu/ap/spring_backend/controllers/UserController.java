package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.jpa.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile operations.
 * Handles retrieving user profile information.
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    
    /**
     * Constructor injection for UserRepository.
     * @param userRepository repository for user data access
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Gets the profile of a user by email.
     * @param email user's email address
     * @return ResponseEntity with UserResponse containing profile data, or 404 if not found
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestParam String email) {
        logger.info("Profile request for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            user.getActiefSinceDate()
        );
        
        logger.info("Profile retrieved for: {}", email);
        return ResponseEntity.ok(response);
    }
}