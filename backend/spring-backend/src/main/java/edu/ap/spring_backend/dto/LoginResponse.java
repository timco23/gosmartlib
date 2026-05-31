package edu.ap.spring_backend.dto;

public class LoginResponse {
    private String name;
    private String firstName;
    private String role;
    private String email;
    private Long schoolId;
    private boolean needsPasswordChange;

    public LoginResponse(String name, String firstName, String role, String email, Long schoolId, boolean needsPasswordChange) {
        this.name = name;
        this.firstName = firstName;
        this.role = role;
        this.email = email;
        this.schoolId = schoolId;
        this.needsPasswordChange = needsPasswordChange;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public boolean isNeedsPasswordChange() { return needsPasswordChange; }
    public void setNeedsPasswordChange(boolean needsPasswordChange) { this.needsPasswordChange = needsPasswordChange; }
}
