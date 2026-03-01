package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class representing a user in the system.
 * Contains user credentials, role, and authentication tokens.
 */
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(unique = true, nullable = false, name = "name")
    private String name;
    
    @Column(unique = true, nullable = false, name = "email")
    private String email;
    
    @Column(nullable = false, name = "password")
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private Role role;
    
    @Column(name = "actief_since_date")
    private LocalDateTime actiefSinceDate;

    @Column(name = "refresh_token")
    private String refreshToken;
    
    /**
     * Default constructor for JPA.
     */
    public User() {}
    
    /**
     * Constructor with all required fields.
     * Automatically sets actiefSinceDate to current time.
     * @param name user's display name
     * @param email user's email address (unique)
     * @param password encrypted password
     * @param role user's role (ADMIN or LEERLING)
     */
    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.actiefSinceDate = LocalDateTime.now();
    }
    
    /**
    * Getters & setters
    */
    public Integer getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public LocalDateTime getActiefSinceDate() { return actiefSinceDate; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}