package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dto.BookStatisticsDTO;
import edu.ap.spring_backend.jpa.CopyStatus;
import edu.ap.spring_backend.jpa.Lending;
import edu.ap.spring_backend.jpa.SchoolItemCopy;
import edu.ap.spring_backend.jpa.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private final LendingRepository lendingRepository;
    private final SchoolItemCopyRepository copyRepository;

    public StatisticsService(LendingRepository repository,
                             SchoolItemCopyRepository copyRepository) {
        this.lendingRepository = repository;
        this.copyRepository = copyRepository;
    }

    public BookStatisticsDTO getBookStatistics(Long bookId, Long schoolId) {
        List<Lending> lendings = lendingRepository.findByBook_Item_IdAndSchool_Id(bookId, schoolId);
        List<SchoolItemCopy> copies = copyRepository.findByItemIdAndSchoolId(bookId, schoolId);

        int totalLendings = lendings.size();
        int activeLendings = (int) lendings.stream()
                .filter(l -> !l.isReturned())
                .count();
        int extensionPercent = totalLendings == 0 ? 0 : (int) Math.round(
                100.0 * lendings.stream().filter(Lending::isExtended).count() / totalLendings);

        List<BookStatisticsDTO.CampusStatisticsDTO> campusStats = buildCampusStats(lendings, copies);

        List<BookStatisticsDTO.RecentLendingDTO> recentLendings = lendings.stream()
                .sorted(Comparator.comparing(Lending::getLendingDate).reversed())
                .limit(10)
                .map(l -> new BookStatisticsDTO.RecentLendingDTO(
                        l.getId(), getBorrowerName(l),
                        l.getLendingDate().toString(), l.getReturnDate().toString(),
                        l.getDateReturned() != null ? l.getDateReturned().toString() : null,
                        l.isReturned(),
                        l.isExtended()))
                .toList();

        return new BookStatisticsDTO(
                totalLendings,
                activeLendings,
                calcAvgDuration(lendings),
                calcLatePercent(lendings),
                extensionPercent,
                calcUnavailableDays(lendings, copies.size()),
                calcMonthlyTrend(lendings, 6),
                campusStats,
                recentLendings);
    }

    // helpers
    private List<BookStatisticsDTO.CampusStatisticsDTO> buildCampusStats(
            List<Lending> lendings, List<SchoolItemCopy> copies) {

        Map<String, List<SchoolItemCopy>> campusMap = copies.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCampus() != null ? c.getCampus().getName() : "Geen locatie"));

        return campusMap.entrySet().stream()
                .map(entry -> {
                    List<SchoolItemCopy> campusCopies = entry.getValue();
                    Set<String> barcodes = campusCopies.stream()
                            .map(SchoolItemCopy::getBarcode)
                            .collect(Collectors.toSet());
                    List<Lending> campusLendings = lendings.stream()
                            .filter(l -> barcodes.contains(l.getBook().getBarcode()))
                            .toList();

                    int campusTotal = campusLendings.size();
                    int campusActive = (int) campusLendings.stream()
                            .filter(l -> !l.isReturned()).count();
                    double lendingsPerCopy = campusCopies.isEmpty() ? 0
                            : Math.round((double) campusTotal / campusCopies.size() * 10.0) / 10.0;
                    int campusAvailableCopies = (int) campusCopies.stream()
                            .filter(c -> c.getCopy_status() == CopyStatus.BESCHIKBAAR).count();

                    return new BookStatisticsDTO.CampusStatisticsDTO(
                            entry.getKey(),
                            campusCopies.size(),
                            campusAvailableCopies,
                            campusTotal,
                            campusActive,
                            calcAvgDuration(campusLendings),
                            lendingsPerCopy,
                            calcUnavailableDays(campusLendings, campusCopies.size()));
                })
                .sorted(Comparator.comparingInt(BookStatisticsDTO.CampusStatisticsDTO::lendingCount).reversed())
                .toList();
    }

    private String getBorrowerName(Lending lending) {
        if (lending.getBorrower() == null) return null;
        User user = lending.getBorrower();
        return user.getFirstName() != null
                ? user.getFirstName() + " " + user.getName()
                : user.getName();
    }

    /**
     * Average lending duration in days over completed lendings with a known return date.
     * Incomplete or date-less lendings are excluded.
     */
    private int calcAvgDuration(List<Lending> lendings) {
        List<Lending> completed = lendings.stream()
                .filter(l -> l.isReturned() && l.getDateReturned() != null)
                .toList();
        if (completed.isEmpty()) return 0;
        return (int) Math.round(
                completed.stream()
                        .mapToLong(l -> ChronoUnit.DAYS.between(l.getLendingDate(), l.getDateReturned()))
                        .average().orElse(0));
    }

    /**
     * Percentage of lendings that were or still are overdue.
     * A lending is overdue when dateReturned > returnDate, or when it is still active past returnDate.
     */
    private int calcLatePercent(List<Lending> lendings) {
        if (lendings.isEmpty()) return 0;
        long lateCount = lendings.stream()
                .filter(l -> {
                    if (l.getDateReturned() != null) return l.getDateReturned().isAfter(l.getReturnDate());
                    return !l.isReturned() && LocalDate.now().isAfter(l.getReturnDate());
                }).count();
        return (int) Math.round(100.0 * lateCount / lendings.size());
    }

    /**
     * Counts the days on which all copies of an item were simultaneously occupied
     * using the sweep-line algorithm.
     *
     * @param lendings    the lendings to analyse (already filtered to a single book + optional campus)
     * @param totalCopies number of physical copies available
     * @return the total number of days all copies were occupied at the same time
     */
    private int calcUnavailableDays(List<Lending> lendings, int totalCopies) {
        if (totalCopies == 0 || lendings.isEmpty()) return 0;
        LocalDate today = LocalDate.now();
        TreeMap<LocalDate, Integer> delta = new TreeMap<>();
        for (Lending l : lendings) {
            LocalDate start = l.getLendingDate();
            LocalDate end = l.getDateReturned() != null ? l.getDateReturned()
                    : l.isReturned() ? l.getReturnDate() : today;

            delta.merge(start, +1, Integer::sum);
            delta.merge(end.plusDays(1), -1, Integer::sum);
        }

        int unavailableDays = 0;
        int activeCount = 0;
        LocalDate prevDate = null;

        for (Map.Entry<LocalDate, Integer> entry : delta.entrySet()) {
            LocalDate date = entry.getKey();

            if (prevDate != null && activeCount >= totalCopies) {
                unavailableDays += (int) ChronoUnit.DAYS.between(prevDate, date);
            }
            activeCount += entry.getValue();
            prevDate = date;
        }
        return unavailableDays;
    }

    /**
     * Lendings counted per month (key format: {@code "YYYY-MM"}), sorted chronologically
     * and trimmed to the {@code months} most recent entries.
     */
    private List<BookStatisticsDTO.MonthlyLendingDTO> calcMonthlyTrend(List<Lending> lendings, int months) {
        Map<String, Long> monthMap = lendings.stream().collect(Collectors.groupingBy(
                l -> l.getLendingDate().getYear() + "-" + String.format("%02d", l.getLendingDate().getMonthValue()),
                Collectors.counting()));
        List<BookStatisticsDTO.MonthlyLendingDTO> result = monthMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new BookStatisticsDTO.MonthlyLendingDTO(e.getKey(), e.getValue().intValue()))
                .collect(Collectors.toList());
        if (result.size() > months) {
            result = result.subList(result.size() - months, result.size());
        }
        return result;
    }
}
