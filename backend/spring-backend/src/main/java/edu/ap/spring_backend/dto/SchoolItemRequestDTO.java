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
        Double minScore,
        List<Long> tagIds
) {}