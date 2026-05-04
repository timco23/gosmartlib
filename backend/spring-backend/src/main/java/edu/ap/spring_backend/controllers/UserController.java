package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.service.UserService;
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
    private final UserService userService;

    /**
     * Constructor with dependency injection.
     * @param userService service for user operations
     * @return a new instance of UserController with the provided UserService
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

    /**
     * Adds a book to the user's favorites list based on the provided email and item ID.
     * @param email
     * @param itemId
     * @return a ResponseEntity with a success message if the book was added, or an error message if the operation failed (e.g. user or item not found, item already in favorites, etc.)
     */
    @PostMapping("/favorites")
    public ResponseEntity<String> addFavoriteBook(@RequestParam String email, @RequestParam Long itemId) {
        try {
            userService.addFavoriteBook(email, itemId);
            return ResponseEntity.ok("Book added to favorites");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Removes a book from the user's favorites list based on the provided email and item ID.
     * @param email
     * @param itemId
     * @return a ResponseEntity with a success message if the book was removed, or an error message if the operation failed (e.g. user or item not found, item not in favorites, etc.)
     */
    @DeleteMapping("/favorites")
    public ResponseEntity<String> deleteFavoriteBook(@RequestParam String email, @RequestParam Long itemId) {
        try {
            userService.deleteFavoriteBook(email, itemId);
            return ResponseEntity.ok("Book removed from favorites");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Updates the role of a user based on their email. Only accessible by ADMIN users.
     * @param email
     * @param role
     * @param auth
     * @return a ResponseEntity containing the updated UserResponse if successful, or an error message if the operation failed (e.g. user not found, invalid role, insufficient permissions, etc.)
     */
    @PutMapping("/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @RequestParam String email,
            @RequestParam String role,
            Authentication auth) {
        UserResponse response = userService.updateUserRole(Integer.parseInt(auth.getName()), email, role);
        return ResponseEntity.ok(response);
    }

    
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