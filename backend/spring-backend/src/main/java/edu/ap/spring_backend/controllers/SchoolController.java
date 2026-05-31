package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.SchoolDTO;
import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.service.SchoolService;
import edu.ap.spring_backend.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/schools")
public class SchoolController {

    private final SchoolService schoolService;
    private final UserService userService;
    private final UserRepository userRepository;

    public SchoolController(SchoolService schoolService, UserService userService, UserRepository userRepository) {
        this.schoolService = schoolService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

     @GetMapping
    public ResponseEntity<List<SchoolDTO>> getAllSchools() {
        return ResponseEntity.ok(
                schoolService.getAllSchools().stream().map(SchoolDTO::from).toList()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<SchoolDTO> getSchoolByName(@RequestParam String name) {
        return ResponseEntity.ok(SchoolDTO.from(schoolService.getSchoolByName(name)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolDTO> getSchoolById(@PathVariable Long id) {
        return ResponseEntity.ok(SchoolDTO.from(schoolService.getSchoolById(id)));
    }

    @PostMapping
    public ResponseEntity<SchoolDTO> createSchool(@RequestBody SchoolDTO school) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SchoolDTO.from(schoolService.createSchool(school.toEntity())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchool(@PathVariable Long id, Authentication auth) {
        User user = resolveUser(auth);
        if (user.getSchool() != null && user.getSchool().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "U kan de school waar u momenteel aan gekoppeld bent niet verwijderen.");
        }
        schoolService.deleteSchool(id);
        return ResponseEntity.noContent().build();
    }

     @PutMapping("/{id}")
    public ResponseEntity<SchoolDTO> updateSchool(@PathVariable Long id, @RequestBody SchoolDTO school) {
        return ResponseEntity.ok(SchoolDTO.from(schoolService.updateSchool(id, school.toEntity())));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsersBySchool(@RequestParam Long schoolId) {
        return ResponseEntity.ok(userService.getUsersBySchool(schoolId));
    }

    /**
     * Returns the school linked to the authenticated user.
     * Accessible by BIBLIOTHEEKBEHEERDER and ADMIN.
     * @param auth the authentication object containing the user's credentials and authorities
     * @return a SchoolDTO representing the school linked to the authenticated user
     * @throws ResponseStatusException with status 404 if the user has no school linked, or 401 if the user cannot be resolved from the authentication context
     */
    @GetMapping("/current")
    public ResponseEntity<SchoolDTO> getCurrentSchool(Authentication auth) {
        User user = resolveUser(auth);
        if (user.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No school linked to this user");
        }
        return ResponseEntity.ok(SchoolDTO.from(schoolService.getSchoolById(user.getSchool().getId())));
    }

    /**
     * Updates the lending settings of the authenticated user's school.
     * Name, address and campuses are not modifiable through this endpoint.
     * Accessible by BIBLIOTHEEKBEHEERDER and ADMIN.
     * @param settings a School object containing the updated settings (only lending-related fields will be applied)
     * @param auth the authentication object containing the user's credentials and authorities
     * @return a SchoolDTO representing the updated school settings
     * @throws ResponseStatusException with status 404 if the user has no school linked, or 401 if the user cannot be resolved from the authentication context
     */
     @PutMapping("/current/settings")
    public ResponseEntity<SchoolDTO> updateCurrentSchoolSettings(@RequestBody SchoolDTO settings, Authentication auth) {
        User user = resolveUser(auth);
        if (user.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No school linked to this user");
        }
        return ResponseEntity.ok(SchoolDTO.from(
                schoolService.updateSchoolSettings(user.getSchool().getId(), settings.toEntity())
        ));
    }

    /**
     * Extracts the authenticated user from the Spring Security context.
     * The principal is stored as the user's numeric ID (set by JwtAuthenticationFilter).
     * @param auth the authentication object containing the user's credentials and authorities
     * @return the User entity corresponding to the authenticated user
     * @throws ResponseStatusException with status 401 if the user cannot be resolved from the authentication context (e.g. invalid principal format or user not found)
     */
    private User resolveUser(Authentication auth) {
        try {
            Integer userId = Integer.valueOf(auth.getName());
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }
    }
}