package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleBooksResponse(
        int totalItems,
        List<GoogleBookItem> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GoogleBookItem(VolumeInfo volumeInfo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VolumeInfo(
            String title,
            List<String> authors,
            String publisher,
            String publishedDate,
            String description,
            int pageCount,
            String language,
            ImageLinks imageLinks) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageLinks(String thumbnail) {
    }
}
