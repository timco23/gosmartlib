package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SchoolItemDTO(
        @JsonProperty("id")
        int id,
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
        @JsonProperty("schoolId")
        int schoolId,
        @JsonProperty("tags")
        List<TagDTO> tags,
        @JsonProperty("description")
        String description,
        @JsonProperty("featured")
        boolean featured,
        @JsonProperty("campusName")
        String campusName,
        @JsonProperty("campusId")
        int campusId
) { }
