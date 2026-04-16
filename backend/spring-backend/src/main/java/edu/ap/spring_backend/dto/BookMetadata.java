package edu.ap.spring_backend.dto;

public record BookMetadata(
        String title,
        String authors,
        String description,
        String language,
        String publisher,
        Integer pageCount,
        Integer releaseYear,
        String coverUrl
) {}
