package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;

/**
 * Entity representing a user in the library system.
 * Users can be administrators or students (leerlingen).
 */
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, name = "name")
    private String name;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(unique = true, nullable = false, name = "email")
    private String email;
    
    @Column(nullable = false, name = "password")
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private Role role;
    
    @Column(name = "student_number")
    private String studentNumber;
    
    @Column(name = "class")
    private String className;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    /**
     * Refresh token for session persistence and future OAuth implementation.
     * Enables "stay logged in" and token refresh without re-authentication.
     */
    @Column(name = "refresh_token")
    private String refreshToken;
    
    public User() {}
    
    /**
     * Constructor for creating admin users.
     * @param name user's last name
     * @param email user's unique email address
     * @param password encrypted password
     * @param role user role (ADMIN or LEERLING)
     */
    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;

    }
    
    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getFirstName() { return firstName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getStudentNumber() { return studentNumber; }
    public String getClassName() { return className; }
    public School getSchool() { return school; }
    public String getRefreshToken() { return refreshToken; }
    
    // Setters
    public void setName(String name) { this.name = name; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public void setClassName(String className) { this.className = className; }
    public void setSchool(School school) { this.school = school; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}