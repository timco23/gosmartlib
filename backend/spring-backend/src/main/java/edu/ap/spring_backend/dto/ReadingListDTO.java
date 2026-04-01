package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ReadingListDTO(
        @JsonProperty("id")
        Long id,
        @JsonProperty("title")
        String title,
        @JsonProperty("description")
        String description,
        @JsonProperty("userFirstName")
        String userFirstName,
        @JsonProperty("userLastName")
        String userLastName,
        @JsonProperty("items")
        List<ItemDTO> items
) {
}
