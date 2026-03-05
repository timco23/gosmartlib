package edu.ap.spring_backend.dto;

/**
 * DTO for changing user password.
 */
public class ChangePasswordRequest {
    private String email;
    private String oldPassword;
    private String newPassword;

    public ChangePasswordRequest() {}

    // Getters
    public String getEmail() { return email; }
    public String getOldPassword() { return oldPassword; }
    public String getNewPassword() { return newPassword; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}