package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.ap.spring_backend.jpa.School;

import java.util.List;

public record CollectionDTO(
        @JsonProperty("id")
        Long id,
        @JsonProperty("title")
        String title,
        @JsonProperty("description")
        String description,
        @JsonProperty("featured")
        Boolean featured,
        @JsonProperty("globallyFeatured")
        Boolean globallyFeatured,
        @JsonProperty("items")
        List<ItemDTO> items,
        @JsonProperty("school")
        School school
) {

}
