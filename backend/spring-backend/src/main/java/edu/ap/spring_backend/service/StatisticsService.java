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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private String getBorrowerName(Lending lending) {
        if (lending.getBorrower() == null)
            return null;

        User user = lending.getBorrower();

        return user.getFirstName() != null
                ? user.getFirstName() + " " + user.getName()
                : user.getName();
    }

    public BookStatisticsDTO getBookStatistics(Long bookId, Long schoolId) {
        List<Lending> lendings = lendingRepository.findByBook_Item_IdAndSchool_Id(bookId, schoolId);
        List<SchoolItemCopy> copies = copyRepository.findByItemIdAndSchoolId(bookId, schoolId);
        LocalDate today = LocalDate.now();

        int totalLendings = lendings.size();
        long activeCount = lendings.stream().filter(l -> !Boolean.TRUE.equals(l.getReturned())).count();
        int activeLendings = (int) activeCount;

        List<Lending> returnedWithDates = lendings.stream()
                .filter(l -> Boolean.TRUE.equals(l.getReturned()) && l.getDateReturned() != null)
                .toList();
        int avgDurationDays = returnedWithDates.isEmpty() ? 0 : (int) Math.round(
                returnedWithDates.stream()
                        .mapToLong(l -> ChronoUnit.DAYS.between(l.getLendingDate(), l.getDateReturned()))
                        .average().orElse(0));

        long lateCount = lendings.stream().filter(l -> {
            if (l.getDateReturned() != null) return l.getDateReturned().isAfter(l.getReturnDate());
            return !Boolean.TRUE.equals(l.getReturned()) && today.isAfter(l.getReturnDate());
        }).count();
        int lateReturnPercent = totalLendings == 0 ? 0 : (int) Math.round(100.0 * lateCount / totalLendings);

        long extendedCount = lendings.stream().filter(l -> Boolean.TRUE.equals(l.getExtended())).count();
        int extensionPercent = totalLendings == 0 ? 0 : (int) Math.round(100.0 * extendedCount / totalLendings);

        int globalUnavailableDays = 0;
        int totalCopies = copies.size();
        if (totalCopies > 0 && !lendings.isEmpty()) {
            LocalDate d = lendings.stream().map(Lending::getLendingDate).min(LocalDate::compareTo).orElse(today);
            while (!d.isAfter(today)) {
                final LocalDate date = d;
                long active = lendings.stream().filter(l -> {
                    LocalDate s = l.getLendingDate();
                    LocalDate e = l.getDateReturned() != null ? l.getDateReturned()
                            : Boolean.TRUE.equals(l.getReturned()) ? l.getReturnDate() : today;
                    return !date.isBefore(s) && !date.isAfter(e);
                }).count();
                if (active >= totalCopies) globalUnavailableDays++;
                d = d.plusDays(1);
            }
        }

        Map<String, Long> monthMap = lendings.stream().collect(Collectors.groupingBy(
                l -> l.getLendingDate().getYear() + "-" + String.format("%02d", l.getLendingDate().getMonthValue()),
                Collectors.counting()));
        List<BookStatisticsDTO.MonthlyLendingDTO> lendingsByMonth = monthMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new BookStatisticsDTO.MonthlyLendingDTO(e.getKey(), e.getValue().intValue()))
                .collect(Collectors.toList());
        if (lendingsByMonth.size() > 6) {
            lendingsByMonth = lendingsByMonth.subList(lendingsByMonth.size() - 6, lendingsByMonth.size());
        }

        Map<String, List<SchoolItemCopy>> campusMap = copies.stream()
                .collect(Collectors.groupingBy(c -> c.getCampus() != null ? c.getCampus().getName() : "Geen locatie"));
        List<BookStatisticsDTO.CampusStatisticsDTO> campusStats = campusMap.entrySet().stream()
                .map(entry -> {
                    List<SchoolItemCopy> campusCopies = entry.getValue();
                    Set<String> barcodes = campusCopies.stream().map(SchoolItemCopy::getBarcode)
                            .collect(Collectors.toSet());
                    List<Lending> campusLendings = lendings.stream()
                            .filter(l -> barcodes.contains(l.getBook().getBarcode())).toList();

                    int campusTotal = campusLendings.size();
                    int campusActive = (int) campusLendings.stream()
                            .filter(l -> !Boolean.TRUE.equals(l.getReturned())).count();
                    List<Lending> campusReturned = campusLendings.stream()
                            .filter(l -> Boolean.TRUE.equals(l.getReturned()) && l.getDateReturned() != null).toList();
                    int campusAvgDuration = campusReturned.isEmpty() ? 0 : (int) Math.round(
                            campusReturned.stream()
                                    .mapToLong(l -> ChronoUnit.DAYS.between(l.getLendingDate(), l.getDateReturned()))
                                    .average().orElse(0));
                    double lendingsPerCopy = campusCopies.isEmpty() ? 0
                            : Math.round((double) campusTotal / campusCopies.size() * 10.0) / 10.0;
                    int campusAvailableCopies = (int) campusCopies.stream()
                            .filter(c -> c.getCopy_status() == CopyStatus.BESCHIKBAAR).count();

                    int campusUnavailableDays = 0;
                    if (!campusLendings.isEmpty()) {
                        LocalDate campusStart = campusLendings.stream().map(Lending::getLendingDate)
                                .min(LocalDate::compareTo).orElse(today);
                        LocalDate d = campusStart;
                        while (!d.isAfter(today)) {
                            final LocalDate date = d;
                            long active = campusLendings.stream().filter(l -> {
                                LocalDate s = l.getLendingDate();
                                LocalDate e = l.getDateReturned() != null ? l.getDateReturned()
                                        : Boolean.TRUE.equals(l.getReturned()) ? l.getReturnDate() : today;
                                return !date.isBefore(s) && !date.isAfter(e);
                            }).count();
                            if (active >= campusCopies.size()) campusUnavailableDays++;
                            d = d.plusDays(1);
                        }
                    }

                    return new BookStatisticsDTO.CampusStatisticsDTO(entry.getKey(), campusCopies.size(),
                            campusAvailableCopies, campusTotal, campusActive, campusAvgDuration,
                            lendingsPerCopy, campusUnavailableDays);
                })
                .sorted(Comparator.comparingInt(BookStatisticsDTO.CampusStatisticsDTO::lendingCount).reversed())
                .toList();

        List<BookStatisticsDTO.RecentLendingDTO> recentLendings = lendings.stream()
                .sorted(Comparator.comparing(Lending::getLendingDate).reversed())
                .limit(10)
                .map(l -> new BookStatisticsDTO.RecentLendingDTO(
                        l.getId(), getBorrowerName(l),
                        l.getLendingDate().toString(), l.getReturnDate().toString(),
                        l.getDateReturned() != null ? l.getDateReturned().toString() : null,
                        Boolean.TRUE.equals(l.getReturned()),
                        Boolean.TRUE.equals(l.getExtended())))
                .toList();

        return new BookStatisticsDTO(totalLendings, activeLendings, avgDurationDays, lateReturnPercent,
                extensionPercent, globalUnavailableDays, lendingsByMonth, campusStats, recentLendings);
    }
}
