package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SmartschoolGroupDTO(
        @JsonProperty("groupID")
        String groupId,
        @JsonProperty("name")
        String name,
        @JsonProperty("description")
        String description,
        @JsonProperty("platform")
        String platform,
        @JsonProperty("instituteNumber")
        String instituteNumber,
        @JsonProperty("adminNumber")
        String adminNumber
) {
}
