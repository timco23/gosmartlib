package edu.ap.spring_backend.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateSchoolItemDTO(
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
        String tags,
        @JsonProperty("description")
        String description,
        @JsonProperty("featured")
        boolean featured
) {
}
