package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OneRosterEnrollmentDTO(
        @JsonProperty("sourcedId")
        String sourcedId,
        @JsonProperty("status")
        String status,
        @JsonProperty("user")
        OneRosterUserDTO user,
        @JsonProperty("role")
        String role,
        @JsonProperty("class")
        OneRosterClassDTO enrollmentClass,
        @JsonProperty("school")
        OneRosterSchoolDTO school,
        @JsonProperty("primary")
        String primary

) {
}
