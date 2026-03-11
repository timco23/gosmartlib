package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.CreateUserRequest;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.dao.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.ap.spring_backend.dto.ChangePasswordRequest;

/**
 * Service for user management operations.
 * Handles student creation with auto-generated credentials.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final String DEFAULT_PASSWORD = "Welkom2024!";
    private static final String EMAIL_DOMAIN = "@gsmartlib.tech";

    public UserService(UserRepository userRepository, SchoolRepository schoolRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new student user with auto-generated email and default password.
     * Email format: {studentNumber}@gsmartlib.tech
     * Password: Welkom2024! (must be changed on first login)
     * @param request student details from admin
     * @return created user entity
     * @throws RuntimeException if school not found or student number already exists
     */
    @Transactional
    public User createStudent(CreateUserRequest request) {
        // Validate school exists
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        // Generate email from student number
        String email = request.getStudentNumber() + EMAIL_DOMAIN;

        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User with student number " + request.getStudentNumber() + " already exists");
        }

        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setName(request.getName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(Role.LEERLING);
        user.setStudentNumber(request.getStudentNumber());
        user.setClassName(request.getClassName());
        user.setSchool(school);
        user.setMustChangePassword(true);

        return userRepository.save(user);
    }

    /**
     * Changes user password after verifying old password.
     * @param request change password request
     * @throws RuntimeException if user not found or old password incorrect
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        
        userRepository.save(user);
    }

    public Item[] getFavoriteBooks(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getFavoriteBooks().toArray(new Item[0]);
    }
}