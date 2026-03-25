package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ItemDTO(
        @JsonProperty("id")
        Long id,
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
        @JsonProperty("description")
        String description,
        @JsonProperty("tags")
        List<TagDTO> tags,
        @JsonProperty("genres")
        List<Long> genres,
        @JsonProperty("subgenres")
        List<Long> subgenres,
        @JsonProperty("readingLevel")
        Long readingLevel,
        @JsonProperty("didacticCategories")
        List<Long> didacticCategories,
        @JsonProperty("schoolId")
        Long schoolId,
        @JsonProperty("featured")
        boolean featured,
        @JsonProperty("isDidactic")
        boolean isDidactic,
        @JsonProperty("campusId")
        Long campusId
) { }