package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides database access methods for user operations.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * Finds a user by their email address.
     * @param email email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by email, ignoring case sensitivity.
     * @param email email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmailIgnoreCase(String email);
    
    /**
     * Checks if a user with the given email exists.
     * @param email email address to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

     /**
     * Finds a user by their Smartschool username.
     * @param smartschoolUsername Smartschool username to search for
     * @return Optional containing the Smartschool username if found, empty otherwise
     */
    Optional<User> findBySmartschoolUsername(String smartschoolUsername);

    /**
     * Finds a user by Smartschool username, ignoring case sensitivity.
     * @param smartschoolUsername Smartschool username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findBySmartschoolUsernameIgnoreCase(String smartschoolUsername);

    /**
     * Checks if a Smartschool username exists.
     * @param smartschoolUsername Smartschool username to check
     * @return true if Smartschool username exists, false otherwise
     */
    boolean existsBySmartschoolUsername(String smartschoolUsername);

    /**
     * Finds a user by refresh token.
     * @param refreshToken refresh token to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByRefreshToken(String refreshToken);
    
    /**
     * Checks if an email exists for another user (excluding given ID).
     * @param email email address to check
     * @param id user ID to exclude
     * @return true if email exists for a different user, false otherwise
     */
    boolean existsByEmailAndIdNot(String email, Integer id);    
}