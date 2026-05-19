package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * Response DTO representing a review.
 * @param id review id
 * @param description review text
 * @param stars rating value
 * @param createdAt creation date
 * @param authorName full name of the review author
 * @param authorEmail email of the review author
 * @param authorSmartschoolUsername full Smartschool name of the author
 * @param isAnonymous is anonymous
 * @param reportCount report count
 */
public record ReviewDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("description") String description,
        @JsonProperty("stars") int stars,
        @JsonProperty("createdAt") LocalDate createdAt,
        @JsonProperty("authorName") String authorName,
        @JsonProperty("authorEmail") String authorEmail,
        @JsonProperty("authorSmartschoolUsername") String authorSmartschoolUsername,
        @JsonProperty("isAnonymous") boolean isAnonymous,
        @JsonProperty("reportCount") int reportCount,
        @JsonProperty("approved") boolean approved,
        @JsonProperty("itemId") Long itemId,
        @JsonProperty("itemTitle") String itemTitle

) {}