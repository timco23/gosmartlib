package edu.ap.spring_backend.dto;

public class LoginResponse {
    private String name;
    private String role;
    private String email;
    private Long schoolId;

    public LoginResponse(String name, String role, String email, Long schoolId) {
        this.name = name;
        this.role = role;
        this.email = email;
        this.schoolId = schoolId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
}
