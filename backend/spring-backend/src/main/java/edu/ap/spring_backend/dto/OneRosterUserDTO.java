package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OneRosterUserDTO(
        @JsonProperty("sourcedId")
        String sourcedId,
        @JsonProperty("identifier")
        String identifier,
        @JsonProperty("status")
        String status,
        @JsonProperty("username")
        String username,
        @JsonProperty("givenName")
        String givenName,
        @JsonProperty("familyName")
        String familyName,
        @JsonProperty("email")
        String email,
        @JsonProperty("role")
        String role,
        @JsonProperty("sex")
        String sex,
        @JsonProperty("orgs")
        OneRosterSchoolDTO[] orgs
) {
}
