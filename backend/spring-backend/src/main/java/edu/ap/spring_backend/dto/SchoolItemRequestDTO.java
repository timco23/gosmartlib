package edu.ap.spring_backend.dto;

import java.util.List;

public record SchoolItemRequestDTO(
        int page,
        int size,
        String sortBy,
        String sortDirection,
        String search,
        List<String> languages,
        Integer minPages,
        Integer maxPages,
        Integer minYear,
        Integer maxYear,
        Double minScore
) {
    public SchoolItemRequestDTO {
        if (sortBy == null) sortBy = "title";
        if (sortDirection == null) sortDirection = "ASC";
        if (page < 0) page = 0;
        if (size < 1 || size > 10) size = 10;
        if (languages == null) languages = List.of();
    }
}