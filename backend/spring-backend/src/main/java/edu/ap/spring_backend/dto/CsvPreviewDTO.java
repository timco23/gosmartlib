package edu.ap.spring_backend.dto;

import java.util.List;

public record CsvPreviewDTO(
        List<String> criticalErrors,
        List<RowError> errors,
        List<BookImportRow> rows) {
}
