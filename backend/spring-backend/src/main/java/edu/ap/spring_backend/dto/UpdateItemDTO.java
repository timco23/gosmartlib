package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UpdateItemDTO(
        @JsonProperty("title")
        String title,
        @JsonProperty("publisher")
        String publisher,
        @JsonProperty("author")
        String author,
        @JsonProperty("releaseYear")
        int releaseYear,
        @JsonProperty("language")
        String bookLanguage,
        @JsonProperty("pageCount")
        int pageCount,
        @JsonProperty("cover")
        String cover,
        @JsonProperty("tags")
        List<Long> tags,
        @JsonProperty("description")
        String description,
        @JsonProperty("isbn")
        String isbn
) {
}
