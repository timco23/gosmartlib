package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.SchoolDTO;
import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.service.SchoolService;
import edu.ap.spring_backend.service.UserService;

import org.springframework.http.HttpStatus;
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
    public List<School> getAllSchools() {
        return schoolService.getAllSchools();
    }

    @GetMapping("/search")
    public School getSchoolByName(@RequestParam String name) {
        return schoolService.getSchoolByName(name);
    }

    @GetMapping("/{id}")
    public School getSchoolById(@PathVariable Long id) {
        return schoolService.getSchoolById(id);
    }

    @PostMapping
    public School createSchool(@RequestBody School school) {
        return schoolService.createSchool(school);
    }

    @DeleteMapping("/{id}")
    public void deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
    }

    @PutMapping("/{id}")
    public School updateSchool(@PathVariable Long id, @RequestBody School school) {
        return schoolService.updateSchool(id, school);
    }

    @GetMapping("/users")
    public List<UserResponse> getUsersBySchool(@RequestParam Long schoolId) {
        return userService.getUsersBySchool(schoolId);
    }

    /**
     * Returns the school linked to the authenticated user.
     * Accessible by BIBLIOTHEEKBEHEERDER and ADMIN.
     * @param auth the authentication object containing the user's credentials and authorities
      * @return a SchoolDTO representing the school linked to the authenticated user
      * @throws ResponseStatusException with status 404 if the user has no school linked, or 401 if the user cannot be resolved from the authentication context
     */
    @GetMapping("/current")
    public SchoolDTO getCurrentSchool(Authentication auth) {
        User user = resolveUser(auth);
        if (user.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No school linked to this user");
        }
        return SchoolDTO.from(schoolService.getSchoolById(user.getSchool().getId()));
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
    public SchoolDTO updateCurrentSchoolSettings(@RequestBody School settings, Authentication auth) {
        User user = resolveUser(auth);
        if (user.getSchool() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No school linked to this user");
        }
        return SchoolDTO.from(schoolService.updateSchoolSettings(user.getSchool().getId(), settings));
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
