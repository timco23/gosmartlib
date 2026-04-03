package edu.ap.spring_backend.dto;

import java.util.List;

public record CsvPreviewDTO(
        List<String> errors,
        List<BookImportRow> rows) {
}
