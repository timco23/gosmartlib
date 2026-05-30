package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OneRosterClassDTO(
        @JsonProperty("sourcedId")
        String sourcedId,
        @JsonProperty("status")
        String status,
        @JsonProperty("title")
        String title,
        @JsonProperty("classCode")
        String classCode,
        @JsonProperty("classType")
        String classType,
        @JsonProperty("school")
        OneRosterSchoolDTO school,
        @JsonProperty("type")
        String type
) {
}
