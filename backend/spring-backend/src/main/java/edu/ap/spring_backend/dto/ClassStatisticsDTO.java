package edu.ap.spring_backend.dto;

import java.util.List;
import java.util.Map;

public record ClassStatisticsDTO(
    Long classId,
    String className,
    int studentCount,
    int totalLendings,
    int activeLendings,
    int lateReturnCount,
    int lateReturnPercent,
    Map<String, Integer> readingLevelDistribution,
    int totalReviews,
    List<StudentStatisticsDTO> students
) {}
