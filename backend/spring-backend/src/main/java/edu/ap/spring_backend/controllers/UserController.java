package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.CreateUserRequest;
import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.ap.spring_backend.dto.ChangePasswordRequest;

/**
 * REST controller for user operations.
 * Handles profile retrieval and student creation.
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final UserService userService;
    
    /**
     * Constructor with dependency injection.
     * @param userRepository repository for user data access
     * @param userService service for user business logic
     */
    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
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
            schoolName,
            user.getActiefSinceDate()
        );
        
        logger.info("Profile retrieved for: {}", email);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new student user.
     * Auto-generates email ({studentNumber}@gsmartlib.tech) and sets default password (Welkom2024!).
     * @param request student details
     * @return created user profile
     * @throws RuntimeException if school not found or student number exists
     */
    @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        logger.info("Creating user with student number: {}", request.getStudentNumber());
        
        User user = userService.createStudent(request);
        
        String schoolName = user.getSchool() != null ? user.getSchool().getName() : null;
        
        UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getFirstName(),
            user.getEmail(),
            user.getRole().name(),
            user.getStudentNumber(),
            user.getClassName(),
            schoolName,
            user.getActiefSinceDate()
        );
        
        logger.info("User created with email: {}", user.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Changes user password.
     * @param request change password request with old and new password
     * @return success message
     * @throws RuntimeException if old password incorrect
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        logger.info("Password change request for: {}", request.getEmail());
        
        try {
            userService.changePassword(request);
            logger.info("Password changed successfully for: {}", request.getEmail());
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            logger.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}