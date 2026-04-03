package edu.ap.spring_backend.dto;

import java.util.List;

public record CsvImportDTO(
        List<String> errors,
        int importedBooks,
        int importedCopies) {
}
