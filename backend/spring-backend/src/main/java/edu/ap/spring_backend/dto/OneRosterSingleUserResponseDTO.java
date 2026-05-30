package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OneRosterSingleUserResponseDTO(
        @JsonProperty("user")
        OneRosterUserDTO user
) {
}
