package edu.ap.spring_backend.dto;

import java.util.List;

public record CsvImportDTO(
        List<RowError> errors,
        int importedBooks,
        int importedCopies,
        List<Integer> copyIds) {
}
