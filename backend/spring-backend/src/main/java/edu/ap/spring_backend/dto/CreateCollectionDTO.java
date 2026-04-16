package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateCollectionDTO(
        @JsonProperty("title")
        String title,
        @JsonProperty("description")
        String description,
        @JsonProperty("schoolId")
        Long schoolId,
        @JsonProperty("featured")
        Boolean featured,
        @JsonProperty("globally_featured")
        Boolean globallyFeatured,
        @JsonProperty("itemIds")
        List<Long> itemIds
) {
}
