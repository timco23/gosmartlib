package edu.ap.spring_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BulkImportHistoryDTO(Long id, LocalDateTime importedAt, int importedBooks, int importedCopies, List<Integer> copyIds) {}
