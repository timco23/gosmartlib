package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OneRosterEnrollmentsResponseDTO(
        @JsonProperty("enrollments")
        OneRosterEnrollmentDTO[] enrollments
) {
}
