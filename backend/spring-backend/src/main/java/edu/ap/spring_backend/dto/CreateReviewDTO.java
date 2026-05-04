package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CreateReviewDTO(
        @JsonProperty("description")
        @Size(max = 800, message = "Description mag maximaal 800 karakters bevatten")
        String description,

        @JsonProperty("stars") @Min(1) @Max(5) int stars,
        @JsonProperty("isAnonymous") boolean isAnonymous
) {}