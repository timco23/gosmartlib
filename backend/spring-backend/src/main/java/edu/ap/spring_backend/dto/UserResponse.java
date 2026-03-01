package edu.ap.spring_backend.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for user profile information.
 * Contains all user data except password for security.
 */
public class UserResponse {
    private Integer id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime actiefSinceDate;
    
    /**
     * Constructor with all fields.
     * @param id user ID
     * @param name user's display name
     * @param email user's email address
     * @param role user's role (ADMIN or LEERLING)
     * @param actiefSinceDate date user became active
     */
    public UserResponse(Integer id, String name, String email, String role, LocalDateTime actiefSinceDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.actiefSinceDate = actiefSinceDate;
    }
    
    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public LocalDateTime getActiefSinceDate() { return actiefSinceDate; }
    public void setActiefSinceDate(LocalDateTime actiefSinceDate) { this.actiefSinceDate = actiefSinceDate; }
}