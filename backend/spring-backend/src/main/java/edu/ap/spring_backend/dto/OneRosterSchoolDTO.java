package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OneRosterSchoolDTO(
        @JsonProperty("sourcedId")
        String sourcedId,
        @JsonProperty("status")
        String status,
        @JsonProperty("name")
        String name,
        @JsonProperty("type")
        String type,
        @JsonProperty("identifier")
        String identifier
) {
}
