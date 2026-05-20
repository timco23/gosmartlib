package edu.ap.spring_backend.dto;

/**
 * DTO for login response.
 * Contains user information after successful authentication.
 */
public class LoginResponse {
    private String name;
    private String role;
    private String email;

    public LoginResponse(String name, String role, String email) {
        this.name = name;
        this.role = role;
        this.email = email;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}