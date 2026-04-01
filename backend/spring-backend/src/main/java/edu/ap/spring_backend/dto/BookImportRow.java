package edu.ap.spring_backend.dto;

public record BookImportRow(
        int rowNumber,
        String title,
        String author,
        String isbn,
        int copies,
        int conditionNieuw,
        int conditionGoed,
        int conditionGebruikt,
        int conditionBeschadigd,
        String campus,
        String language,
        String description) {
}
