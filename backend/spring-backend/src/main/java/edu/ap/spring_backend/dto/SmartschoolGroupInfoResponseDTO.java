package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SmartschoolGroupInfoResponseDTO(
        @JsonProperty("groups")
        SmartschoolGroupDTO[] groups,
        @JsonProperty("parentGroups")
        SmartschoolGroupDTO[] parentGroups
) {
}
