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
     * Checks if a Smartschool username exists.
     * @param smartschoolUsername Smartschool username to check
     * @return true if Smartschool username exists, false otherwise
     */
    boolean existsBySmartschoolUsername(String smartschoolUsername);

    /**
     * Checks if a refresh token exists.
     * @param refreshToken Refresh token to check
     * @return true if refresh token exists, false otherwise
     */
    Optional<User> findByRefreshToken(String refreshToken);
}