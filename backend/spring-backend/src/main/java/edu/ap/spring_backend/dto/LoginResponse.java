package edu.ap.spring_backend.dto;

/**
 * Data Transfer Object for login responses.
 * Contains authenticated user information.
 */
public class LoginResponse {
    private String name;
    private String role;
    
    /**
     * Constructor for LoginResponse.
     * @param name user's name
     * @param role user's role (ADMIN or LEERLING)
     */
    public LoginResponse(String name, String role) {
        this.name = name;
        this.role = role;
    }
    
    /**
     * Gets the user's name.
     * @return user name
     */
    public String getName() { return name; }
    
    /**
     * Gets the user's role.
     * @return user role
     */
    public String getRole() { return role; }
}