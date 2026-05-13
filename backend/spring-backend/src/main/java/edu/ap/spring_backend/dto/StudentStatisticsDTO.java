package edu.ap.spring_backend.dto;

public record StudentStatisticsDTO(
    Long userId,
    String fullName,
    int totalLendings,
    int activeLendings,
    int lateReturnCount,
    int lateReturnPercent,
    String avgReadingLevel,
    int reviewCount
) {}
