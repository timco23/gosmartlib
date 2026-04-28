package edu.ap.spring_backend.dto;

import java.util.List;

public record CsvPreviewDTO(
        List<String> criticalErrors,
        List<String> errors,
        List<BookImportRow> rows) {
}
