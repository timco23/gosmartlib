package edu.ap.spring_backend.dto;

import edu.ap.spring_backend.jpa.Genre;

public record GenreDTO(Long id, String name) {
    public static GenreDTO from(Genre genre) {
        if (genre == null) return null;
        return new GenreDTO(genre.getId(), genre.getName());
    }
}