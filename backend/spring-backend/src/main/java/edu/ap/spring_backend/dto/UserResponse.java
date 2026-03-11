package edu.ap.spring_backend.dto;

import java.time.LocalDateTime;

/**
 * DTO for user profile information without sensitive data.
 * Includes student-specific fields if applicable.
 */
public class UserResponse {
    private Integer id;
    private String name;
    private String firstName;
    private String email;
    private String role;
    private String studentNumber;
    private String className;
    private String schoolName;
    private LocalDateTime actiefSinceDate;
    
    /**
     * Constructor for user profile response.
     * @param id user ID
     * @param name user's last name
     * @param firstName user's first name
     * @param email user's email
     * @param role user role (ADMIN, LEERLING, LEERKRACHT or BEHEERDER)
     * @param studentNumber student number (null for admins)
     * @param className class name (null for admins)
     * @param schoolName school name (null for admins)
     * @param actiefSinceDate account creation date
     */
    public UserResponse(Integer id, String name, String firstName, String email, String role, 
                       String studentNumber, String className, String schoolName, LocalDateTime actiefSinceDate) {
        this.id = id;
        this.name = name;
        this.firstName = firstName;
        this.email = email;
        this.role = role;
        this.studentNumber = studentNumber;
        this.className = className;
        this.schoolName = schoolName;
        this.actiefSinceDate = actiefSinceDate;
    }
    
    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getFirstName() { return firstName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStudentNumber() { return studentNumber; }
    public String getClassName() { return className; }
    public String getSchoolName() { return schoolName; }
    public LocalDateTime getActiefSinceDate() { return actiefSinceDate; }
    
    // Setters
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public void setClassName(String className) { this.className = className; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public void setActiefSinceDate(LocalDateTime actiefSinceDate) { this.actiefSinceDate = actiefSinceDate; }
}