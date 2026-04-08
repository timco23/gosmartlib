package edu.ap.spring_backend.dto;

import java.util.List;

public record ItemRequestDTO(
        int page,
        int size,
        String sortBy,
        String sortDirection,
        String search,
        List<String> languages,
        Integer minPages,
        Integer maxPages,
        Double minScore,
        List<Long> tagIds,
        List<Long> genreIds,
        List<Long> subgenreIds,
        List<Long> didacticCategoryIds,
        Boolean didactic,
        Long schoolId
) {}