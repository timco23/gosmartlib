package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ReviewDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("description") String description,
        @JsonProperty("stars") int stars,
        @JsonProperty("createdAt") LocalDateTime createdAt,
        @JsonProperty("authorName") String authorName,
        @JsonProperty("authorEmail") String authorEmail,
        @JsonProperty("authorSmartschoolUsername") String authorSmartschoolUsername,
        @JsonProperty("isAnonymous") boolean isAnonymous,
        @JsonProperty("reportCount") int reportCount,
        @JsonProperty("approved") boolean approved,
        @JsonProperty("itemId") Long itemId,
        @JsonProperty("itemTitle") String itemTitle

) {}