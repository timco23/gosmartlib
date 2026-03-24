package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReadingLevelDTO(
    @JsonProperty("id")
    int id,

    @JsonProperty("level")
    String level
) {}