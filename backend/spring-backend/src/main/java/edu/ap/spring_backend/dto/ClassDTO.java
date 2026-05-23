package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClassDTO(
        @JsonProperty("id")
        Long id,
        @JsonProperty("className")
        String className,
        @JsonProperty("schoolId")
        Long schoolId
) {
}
