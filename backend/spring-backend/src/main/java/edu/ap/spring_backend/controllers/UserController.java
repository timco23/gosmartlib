package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.dao.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user operations.
 * Handles profile retrieval and student creation.
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    
    /**
     * Constructor with dependency injection.
     * @param userRepository repository for user data access
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Retrieves user profile by email.
     * @param email user's email address
     * @return user profile data without password
     * @throws RuntimeException if user not found
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestParam String email) {
        logger.info("Profile request for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String schoolName = user.getSchool() != null ? user.getSchool().getName() : null;
        
        UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getFirstName(),
            user.getEmail(),
            user.getRole().name(),
            user.getStudentNumber(),
            user.getClassName(),
            schoolName
        );
        
        logger.info("Profile retrieved for: {}", email);
        return ResponseEntity.ok(response);
    }
}