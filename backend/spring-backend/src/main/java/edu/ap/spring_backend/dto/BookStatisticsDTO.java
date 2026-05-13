package edu.ap.spring_backend.dto;

import java.util.List;

public record BookStatisticsDTO(
    int totalLendings,
    int activeLendings,
    int avgDurationDays,
    int lateReturnPercent,
    int extensionPercent,
    int globalUnavailableDays,
    List<MonthlyLendingDTO> lendingsByMonth,
    List<CampusStatisticsDTO> campusStats,
    List<RecentLendingDTO> recentLendings,
    SchoolRankingDTO schoolRanking
) {
    public record MonthlyLendingDTO(String month, int count) {}

    public record CampusStatisticsDTO(
        String name,
        int totalCopies,
        int availableCopies,
        int lendingCount,
        int activeLendings,
        int avgDuration,
        double lendingsPerCopy,
        int unavailableDays
    ) {}

    public record RecentLendingDTO(
        Long id,
        String borrowerName,
        String lendingDate,
        String returnDate,
        String dateReturned,
        boolean returned,
        boolean extended
    ) {}

    public record SchoolRankingDTO(
        int rankAbsolute,
        int rankPerCopy,
        Integer rankReadingLevel,       // null when book has no reading level
        int totalBooks,
        Integer totalBooksReadingLevel, // null when book has no reading level
        double lendingsPerCopy
    ) {}
}
