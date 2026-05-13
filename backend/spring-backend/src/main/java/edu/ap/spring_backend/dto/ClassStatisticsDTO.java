package edu.ap.spring_backend.dto;

import java.util.List;

public record ClassStatisticsDTO(
    Long classId,
    String className,
    int studentCount,
    int totalLendings,
    int activeLendings,
    int lateReturnCount,
    int lateReturnPercent,
    String avgReadingLevel,
    int totalReviews,
    List<StudentStatisticsDTO> students
) {}
