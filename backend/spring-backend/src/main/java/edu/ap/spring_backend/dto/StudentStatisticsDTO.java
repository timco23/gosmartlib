package edu.ap.spring_backend.dto;

import java.util.List;
import java.util.Map;

public record StudentStatisticsDTO(
    Long userId,
    String fullName,
    int totalLendings,
    int activeLendings,
    int lateReturnCount,
    int lateReturnPercent,
    int reviewCount,
    Map<String, Integer> readingLevelDistribution,
    List<String> topGenres
) {}
