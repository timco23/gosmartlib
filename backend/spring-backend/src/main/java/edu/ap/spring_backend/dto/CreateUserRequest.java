package edu.ap.spring_backend.dto;

/**
 * DTO for creating a new student user.
 * Admin provides student details, system generates email and password.
 */
public class CreateUserRequest {
    private String firstName;
    private String name;
    private String studentNumber;
    private String className;
    private Long schoolId;

    public CreateUserRequest() {}

    // Getters
    public String getFirstName() { return firstName; }
    public String getName() { return name; }
    public String getStudentNumber() { return studentNumber; }
    public String getClassName() { return className; }
    public Long getSchoolId() { return schoolId; }

    // Setters
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setName(String name) { this.name = name; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public void setClassName(String className) { this.className = className; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
}