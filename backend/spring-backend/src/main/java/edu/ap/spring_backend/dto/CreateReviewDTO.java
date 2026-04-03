package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO used to create a new review.
 * @param title review title (max 80 characters)
 * @param description review text (max 800 characters)
 * @param stars rating value from 1 to 5
 */
public record CreateReviewDTO(
        @JsonProperty("title")
        @Size(max = 80, message = "Title mag maximaal 80 karakters bevatten")
        String title,

        @JsonProperty("description")
        @Size(max = 800, message = "Description mag maximaal 800 karakters bevatten")
        String description,

        @JsonProperty("stars") @Min(1) @Max(5) int stars
) {}