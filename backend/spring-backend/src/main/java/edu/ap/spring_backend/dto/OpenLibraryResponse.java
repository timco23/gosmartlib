package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibraryResponse(
        String title,
        List<Author> authors,
        @JsonProperty("number_of_pages") Integer numberOfPages,
        List<Publisher> publishers,
        @JsonProperty("publish_date") String publishDate,
        Cover cover
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Author(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Publisher(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cover(String small, String medium, String large) {}
}
