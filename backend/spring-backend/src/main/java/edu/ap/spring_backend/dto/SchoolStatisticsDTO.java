package edu.ap.spring_backend.dto;

import java.util.List;

public record SchoolStatisticsDTO(
    int totalLendings,
    int activeLendings,
    int avgDurationDays,
    int lateReturnCount,
    int lateReturnPercent,
    int extensionPercent,
    int totalReviews,
    List<CampusStatsDTO> campusStats,
    List<RecentLendingDTO> recentLendings,
    List<PeriodCountDTO> lendingsTrend,
    List<TopBookDTO> topBorrowedBooks,
    List<TopBookDTO> topUnavailableBooks
) {
    public record CampusStatsDTO(
        String name,
        int totalLendings,
        int activeLendings,
        int avgDurationDays,
        int lateReturnCount,
        int lateReturnPercent,
        int extensionPercent
    ) {}

    public record RecentLendingDTO(
        Long id,
        String borrowerName,
        String bookTitle,
        String lendingDate,
        String returnDate,
        String dateReturned,
        boolean returned,
        boolean extended
    ) {}

    public record PeriodCountDTO(String period, int count) {}

    public record TopBookDTO(Long itemId, String title, int value) {}
}
