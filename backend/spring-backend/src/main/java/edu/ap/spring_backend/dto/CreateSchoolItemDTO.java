package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateSchoolItemDTO(
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
        @JsonProperty(value = "schoolId")
        int schoolId,
        @JsonProperty(value = "tags")
        List<Long> tags,
        @JsonProperty(value = "description")
        String description,
        @JsonProperty(value = "featured")
        boolean featured,
        @JsonProperty("campusId")
        int campusId,
        @JsonProperty("genres")
        List<Long> genres,
        @JsonProperty("subgenres")
        List<Long> subgenres,
        @JsonProperty("readingLevel")
        Long readingLevel,
        @JsonProperty("didacticCategories")
        List<Long> didacticCategories,
        @JsonProperty("isDidactic")
        boolean isDidactic
) { }
