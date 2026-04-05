package edu.ap.spring_backend.dto;

import java.util.List;

public record CsvConfirmDTO(
        Long schoolId,
        List<BookImportRow> rows) {
}
