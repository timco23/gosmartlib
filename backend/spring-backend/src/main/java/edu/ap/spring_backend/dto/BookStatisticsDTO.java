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
    List<RecentLendingDTO> recentLendings
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
}
