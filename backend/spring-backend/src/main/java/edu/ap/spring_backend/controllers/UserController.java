package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.service.ItemService;
import edu.ap.spring_backend.service.UserService;
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
    private final UserService userService;
    public final ItemService itemService;
    
    /**
     * Constructor with dependency injection.
     * @param userRepository repository for user data access
     */
    public UserController(UserRepository userRepository, UserService userService, ItemService itemService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.itemService = itemService;
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
            user.getFavoriteBooks().toArray(new Item[0])
        );
        
        logger.info("Profile retrieved for: {}", email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites")
    public ResponseEntity<Item[]> getFavoriteBooks(@RequestParam String email) {
        Item[] favoriteBooks = userService.getFavoriteBooks(email);
        return ResponseEntity.ok(favoriteBooks);
    }

    @PostMapping("/favorites")
    public ResponseEntity<String> addFavoriteBook(@RequestParam String email, @RequestParam Integer itemId) {
        try {
            userService.addFavoriteBook(email, itemId);
            return ResponseEntity.ok("Book added to favorites");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/favorites")
    public ResponseEntity<String> deleteFavoriteBook(@RequestParam String email, @RequestParam Integer itemId) {
        try {
            userService.deleteFavoriteBook(email, itemId);
            return ResponseEntity.ok("Book removed from favorites");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}