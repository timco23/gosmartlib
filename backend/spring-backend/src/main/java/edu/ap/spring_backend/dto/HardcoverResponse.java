package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HardcoverResponse(Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(List<Edition> editions) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Edition(
            Integer pages,
            Language language,
            Publisher publisher,
            Image image,
            Book book
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Language(String language) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Publisher(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(String url) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Book(
            String title,
            @JsonProperty("release_year") Integer releaseYear,
            String description,
            List<Contribution> contributions
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Contribution(Author author) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Author(String name) {}
}
