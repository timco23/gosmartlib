package edu.ap.spring_backend.dto;

/**
 * Data Transfer Object for login requests.
 * Contains user credentials for authentication.
 */
public class LoginRequest {
    private String email;
    private String password;
    
    /**
     * Gets the user's email address.
     * @return email address
     */
    public String getEmail() { return email; }

     /**
     * Sets the user's email address.
     * @param email email address to set
     */
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}