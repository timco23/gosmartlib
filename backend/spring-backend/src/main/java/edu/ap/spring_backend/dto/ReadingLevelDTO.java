package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReadingLevelDTO(
    @JsonProperty("id")
    Long id,

    @JsonProperty("level")
    String level,

    @JsonProperty("description")
    String description
) {}