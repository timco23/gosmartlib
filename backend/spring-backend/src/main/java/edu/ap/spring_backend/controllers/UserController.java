package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * REST controller for user operations.
 * Handles profile retrieval and student creation.
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * Constructor with dependency injection.
     *
     * @param userService service for user operations
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getOwnProfile(Authentication auth) {
        UserResponse response = userService.getOwnProfile(Integer.parseInt(auth.getName()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<UserResponse> getProfileByEmail(@PathVariable String email) {
        UserResponse response = userService.getProfile(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/school-users")
    public ResponseEntity<List<UserResponse>> getSchoolUsers(Authentication auth) {
        List<UserResponse> users = userService.getUsersByCurrentSchool(Integer.parseInt(auth.getName()));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/favorites")
    public ResponseEntity<ItemDTO[]> getFavoriteBooks(@RequestParam String email) {
        ItemDTO[] favoriteBooks = userService.getFavoriteBooks(email);
        return ResponseEntity.ok(favoriteBooks);
    }

    @PostMapping("/favorites")
    public ResponseEntity<String> addFavoriteBook(@RequestParam String email, @RequestParam Long itemId) {
        try {
            userService.addFavoriteBook(email, itemId);
            return ResponseEntity.ok("Book added to favorites");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/favorites")
    public ResponseEntity<String> deleteFavoriteBook(@RequestParam String email, @RequestParam Long itemId) {
        try {
            userService.deleteFavoriteBook(email, itemId);
            return ResponseEntity.ok("Book removed from favorites");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin-only
    @PutMapping("/school")
    public ResponseEntity<UserResponse> changeSchool(@RequestParam String email, @RequestParam Long schoolId) {
        try {
            UserResponse response = userService.changeUserSchool(email, schoolId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/addBorrowed")
    public ResponseEntity<UserResponse> addBorrowedItem(@RequestParam Integer userId) {
        try {
            UserResponse response = userService.addToItemsBorrowed(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/removeBorrowed")
    public ResponseEntity<UserResponse> removeBorrowedItem(@RequestParam Integer userId) {
        try {
            UserResponse response = userService.removeFromItemsBorrowed(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}