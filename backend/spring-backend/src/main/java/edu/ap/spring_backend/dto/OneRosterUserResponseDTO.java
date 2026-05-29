package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OneRosterUserResponseDTO(
        @JsonProperty("users")
        OneRosterUserDTO[] users
) {
}
