package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ClassRepository;
import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dao.ReviewRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.BookStatisticsDTO;
import edu.ap.spring_backend.dto.ClassStatisticsDTO;
import edu.ap.spring_backend.dto.ItemCountDTO;
import edu.ap.spring_backend.dto.StudentStatisticsDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.CopyStatus;
import edu.ap.spring_backend.jpa.Lending;
import edu.ap.spring_backend.jpa.ReadingLevel;
import edu.ap.spring_backend.jpa.SchoolClass;
import edu.ap.spring_backend.jpa.SchoolItemCopy;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.util.SchoolYearUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private final LendingRepository lendingRepository;
    private final SchoolItemCopyRepository copyRepository;
    private final ReviewRepository reviewRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    public StatisticsService(LendingRepository repository,
                             SchoolItemCopyRepository copyRepository,
                             ReviewRepository reviewRepository,
                             ClassRepository classRepository,
                             UserRepository userRepository) {
        this.lendingRepository = repository;
        this.copyRepository = copyRepository;
        this.reviewRepository = reviewRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
    }

    public BookStatisticsDTO getBookStatistics(Long bookId, Long schoolId, String groupBy, int trendLimit) {
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
                calcTrend(lendings, groupBy, trendLimit),
                campusStats,
                recentLendings,
                calcSchoolRanking(bookId, schoolId, copies));
    }

    public StudentStatisticsDTO getStudentStatistics(Integer userId, Long schoolId) {
        User user = userRepository.findById(userId)
                .orElseThrow(EntityNotFoundException::new);
        List<Lending> lendings = lendingRepository.findByBorrower_IdAndSchool_Id(userId, schoolId);
        return buildStudentStats(user, lendings);
    }

    public ClassStatisticsDTO getClassStatistics(Long classId, Long schoolId) {
        SchoolClass sc = classRepository.findById(classId)
                .orElseThrow(EntityNotFoundException::new);

        List<Lending> allLendings = lendingRepository.findBySnapClassNameAndSchool_Id(sc.getClassName(), schoolId);

        Map<Integer, List<Lending>> byUser = allLendings.stream()
                .filter(l -> l.getBorrower() != null)
                .collect(Collectors.groupingBy(l -> l.getBorrower().getId()));

        List<StudentStatisticsDTO> students = sc.getUsers().stream()
                .map(u -> buildStudentStats(u, byUser.getOrDefault(u.getId(), List.of())))
                .sorted(Comparator.comparing(StudentStatisticsDTO::fullName))
                .toList();

        List<Lending> currentYearLendings = filterBySchoolYear(allLendings, null);
        Map<String, Integer> readingLevelDistribution = readingLevelDistribution(currentYearLendings);

        return new ClassStatisticsDTO(
                classId,
                sc.getClassName(),
                sc.getUsers().size(),
                allLendings.size(),
                (int) allLendings.stream().filter(l -> !l.isReturned()).count(),
                (int) calcLateCount(allLendings),
                calcLatePercent(allLendings),
                readingLevelDistribution,
                students.stream().mapToInt(StudentStatisticsDTO::reviewCount).sum(),
                students);
    }

    private StudentStatisticsDTO buildStudentStats(User user, List<Lending> lendings) {
        String fullName = user.getFirstName() != null
                ? user.getFirstName() + " " + user.getName()
                : user.getName();

        List<Lending> currentYearLendings = filterBySchoolYear(lendings, null);
        Map<String, Integer> readingLevelDistribution = readingLevelDistribution(currentYearLendings);

        List<String> topGenres = currentYearLendings.stream()
                .map(Lending::getSnapBookGenre)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(s -> s, Collectors.summingInt(x -> 1)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        return new StudentStatisticsDTO(
                Long.valueOf(user.getId()),
                fullName,
                lendings.size(),
                (int) lendings.stream().filter(l -> !l.isReturned()).count(),
                (int) calcLateCount(lendings),
                calcLatePercent(lendings),
                (int) reviewRepository.countByAuthor_Id(user.getId()),
                readingLevelDistribution,
                topGenres);
    }

    // helpers
    private List<Lending> filterBySchoolYear(List<Lending> lendings, String schoolYear) {
        String year = (schoolYear != null) ? schoolYear : SchoolYearUtil.determineSchoolYear(LocalDate.now());
        return lendings.stream()
                .filter(l -> year.equals(l.getSnapSchoolYear()))
                .toList();
    }

    private Map<String, Integer> readingLevelDistribution(List<Lending> lendings) {
        return lendings.stream()
                .map(Lending::getSnapReadingLevel)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(s -> s, Collectors.summingInt(x -> 1)));
    }

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

    /** Number of lendings that were or still are overdue. */
    private long calcLateCount(List<Lending> lendings) {
        return lendings.stream()
                .filter(l -> {
                    if (l.getDateReturned() != null) return l.getDateReturned().isAfter(l.getReturnDate());
                    return !l.isReturned() && LocalDate.now().isAfter(l.getReturnDate());
                }).count();
    }

    /**
     * Percentage of lendings that were or still are overdue.
     * A lending is overdue when dateReturned > returnDate, or when it is still active past returnDate.
     */
    private int calcLatePercent(List<Lending> lendings) {
        if (lendings.isEmpty()) return 0;
        return (int) Math.round(100.0 * calcLateCount(lendings) / lendings.size());
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
     * Computes three independent rankings for the given book within its school library.
     * Absolute: ranked by total lending count across all books.
     * Per copy: ranked by lendingsPerCopy
     * Reading level: ranked by absolute lendings within books that share
     *       the same reading level. null when the book has no reading level assigned.
     *
     * @param copies preloaded copies for this book, used to extract its reading level
     */
    private BookStatisticsDTO.SchoolRankingDTO calcSchoolRanking(Long bookId, Long schoolId,
                                                                  List<SchoolItemCopy> copies) {
        Long readingLevelId = copies.stream()
                .map(c -> c.getItem().getReadingLevel())
                .filter(Objects::nonNull)
                .map(ReadingLevel::getId)
                .findFirst().orElse(null);

        Map<Long, Long> lendingsByItem = lendingRepository.countLendingsByItemForSchool(schoolId)
                .stream().collect(Collectors.toMap(ItemCountDTO::itemId, ItemCountDTO::count));
        Map<Long, Long> copiesByItem = copyRepository.countCopiesByItemForSchool(schoolId)
                .stream().collect(Collectors.toMap(ItemCountDTO::itemId, ItemCountDTO::count));

        int totalBooks = copiesByItem.size();
        Set<Long> allItemIds = copiesByItem.keySet();

        // ranking 1: absolute lendings
        List<Long> byAbsolute = allItemIds.stream()
                .sorted(Comparator.comparingLong((Long id) -> lendingsByItem.getOrDefault(id, 0L)).reversed())
                .toList();
        int rankAbsolute = positionOf(bookId, byAbsolute, totalBooks);

        // ranking 2: per copy
        List<Long> byIntensity = allItemIds.stream()
                .sorted(Comparator.comparingDouble((Long id) -> lendingIntensityPerCopy(id, lendingsByItem, copiesByItem)).reversed())
                .toList();
        int rankPerCopy = positionOf(bookId, byIntensity, totalBooks);

        long currentCopies = copiesByItem.getOrDefault(bookId, 1L);
        long currentLendings = lendingsByItem.getOrDefault(bookId, 0L);
        double lendingsPerCopy = Math.round((double) currentLendings / currentCopies * 10.0) / 10.0;

        // ranking 3: absolute lendings within same reading level
        Integer rankReadingLevel = null;
        Integer totalBooksReadingLevel = null;
        if (readingLevelId != null) {
            List<Long> levelIds = new ArrayList<>(
                    copyRepository.findItemIdsBySchoolAndReadingLevel(schoolId, readingLevelId));
            Map<Long, Long> levelLendings = lendingRepository
                    .countLendingsByItemForSchoolAndReadingLevel(schoolId, readingLevelId)
                    .stream().collect(Collectors.toMap(ItemCountDTO::itemId, ItemCountDTO::count));

            totalBooksReadingLevel = levelIds.size();
            levelIds.sort((a, b) -> Long.compare(
                    levelLendings.getOrDefault(b, 0L),
                    levelLendings.getOrDefault(a, 0L)));
            rankReadingLevel = positionOf(bookId, levelIds, totalBooksReadingLevel);
        }

        return new BookStatisticsDTO.SchoolRankingDTO(
                rankAbsolute, rankPerCopy, rankReadingLevel,
                totalBooks, totalBooksReadingLevel, lendingsPerCopy);
    }

    private static int positionOf(Long id, List<Long> rankedIds, int total) {
        int idx = rankedIds.indexOf(id);
        return idx >= 0 ? idx + 1 : total;
    }

    // Lendings per copy for a single item; defaults to 0 lendings and 1 copy when absent.
    private static double lendingIntensityPerCopy(Long itemId, Map<Long, Long> lendingCounts, Map<Long, Long> copyCounts) {
        return (double) lendingCounts.getOrDefault(itemId, 0L) / copyCounts.getOrDefault(itemId, 1L);
    }

    /**
     * Lendings grouped by month ({@code "YYYY-MM"}) or year ({@code "YYYY"}), sorted
     * chronologically. When {@code limit > 0} the result is trimmed to the most recent entries.
     */
    private List<BookStatisticsDTO.MonthlyLendingDTO> calcTrend(List<Lending> lendings, String groupBy, int limit) {
        Map<String, Long> map = lendings.stream().collect(Collectors.groupingBy(
                "year".equalsIgnoreCase(groupBy)
                        ? l -> String.valueOf(l.getLendingDate().getYear())
                        : l -> l.getLendingDate().getYear() + "-" + String.format("%02d", l.getLendingDate().getMonthValue()),
                Collectors.counting()));
        List<BookStatisticsDTO.MonthlyLendingDTO> result = map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new BookStatisticsDTO.MonthlyLendingDTO(e.getKey(), e.getValue().intValue()))
                .collect(Collectors.toList());
        if (limit > 0 && result.size() > limit) {
            result = result.subList(result.size() - limit, result.size());
        }
        return result;
    }
}
