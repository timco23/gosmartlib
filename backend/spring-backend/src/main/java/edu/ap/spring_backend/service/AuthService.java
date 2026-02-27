package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dto.LoginRequest;
import edu.ap.spring_backend.dto.LoginResponse;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.jpa.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class handling authentication logic.
 * Validates user credentials and returns authenticated user information.
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Constructor injection for dependencies.
     * @param userRepository repository for user data access
     * @param passwordEncoder encoder for password validation
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Authenticates a user with email and password.
     * @param request LoginRequest containing email and password
     * @return LoginResponse with user name and role
     * @throws RuntimeException if user not found or password incorrect
     */

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User cannot be found."));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password.");
        }
        
        return new LoginResponse(user.getName(), user.getRole().name());
    }
}