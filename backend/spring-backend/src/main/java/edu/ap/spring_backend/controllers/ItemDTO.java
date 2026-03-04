package edu.ap.spring_backend.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemDTO(
        @JsonProperty("title")
        String title,
        @JsonProperty("publisher")
        String publisher,
        @JsonProperty("author")
        String author,
        @JsonProperty("isbn")
        String isbn,
        @JsonProperty("releaseYear")
        int releaseYear,
        @JsonProperty("language")
        String bookLanguage,
        @JsonProperty("pageCount")
        int pageCount,
        @JsonProperty("cover")
        String cover,
        @JsonProperty("reviewScore")
        double reviewScore,
        @JsonProperty("reviewCount")
        int reviewCount,
        @JsonProperty("itemId")
        int itemId,
        @JsonProperty(value = "schoolId", required = true)
        int schoolId,
        @JsonProperty(value = "tags", required = true)
        String tags,
        @JsonProperty(value = "description", required = true)
        String description,
        @JsonProperty(value = "featured", required = true)
        boolean featured
) { }
