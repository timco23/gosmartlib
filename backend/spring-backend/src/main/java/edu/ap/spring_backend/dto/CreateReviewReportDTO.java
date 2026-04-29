package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record CreateReviewReportDTO(
        @JsonProperty("reason")
        @Size(max = 800, message = "Reden mag maximaal 800 karakters bevatten")
        String reason
) {}