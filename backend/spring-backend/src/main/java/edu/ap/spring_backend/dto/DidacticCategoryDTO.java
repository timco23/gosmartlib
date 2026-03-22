package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DidacticCategoryDTO(
    @JsonProperty("id") 
    int id,

    @JsonProperty("name")
    String name
) {}
