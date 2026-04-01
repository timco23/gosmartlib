package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateReadingListDTO(
        @JsonProperty("title")
        String title,
        @JsonProperty("description")
        String description,
        @JsonProperty("userId")
        int userId,
        @JsonProperty("itemIds")
        List<Long> itemIds
) {
}
