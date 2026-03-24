package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.ap.spring_backend.jpa.CopyStatus;
import edu.ap.spring_backend.jpa.ItemCondition;

import java.util.List;

public record CreateItemDTO(
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
        Long schoolId,
        @JsonProperty(value = "tags")
        List<Long> tags,
        @JsonProperty(value = "description")
        String description,
        @JsonProperty(value = "featured")
        boolean featured,
        @JsonProperty("campusId")
        Long campusId,
        @JsonProperty("barcode")
        String barcode,
        @JsonProperty("copy_status")
        CopyStatus copyStatus,
        @JsonProperty("condition")
        ItemCondition condition) { }
