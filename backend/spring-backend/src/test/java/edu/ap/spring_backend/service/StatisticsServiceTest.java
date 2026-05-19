package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ClassRepository;
import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dao.ReviewRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.BookStatisticsDTO;
import edu.ap.spring_backend.dto.ClassStatisticsDTO;
import edu.ap.spring_backend.dto.ItemCountDTO;
import edu.ap.spring_backend.jpa.*;
import edu.ap.spring_backend.util.SchoolYearUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsServiceTest {

    @Mock private LendingRepository lendingRepository;
    @Mock private SchoolItemCopyRepository copyRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ClassRepository classRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private StatisticsService service;

    private static final Long BOOK_ID = 9L;
    private static final Long SCHOOL_ID = 1L;

    private Item item;
    private SchoolItemCopy copy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        item = new Item();
        item.setId(BOOK_ID);
        item.setTitle("Dune");
        item.setAuthor("Frank Herbert");

        copy = new SchoolItemCopy();
        copy.setBarcode("BC001");
        copy.setItem(item);
        copy.setCopy_status(CopyStatus.BESCHIKBAAR);

        stubRankingRepositories(List.of(), List.of());
    }

    // --- helpers ---

    private Lending completedLending(String barcode, LocalDate start, LocalDate returnDate, LocalDate dateReturned) {
        SchoolItemCopy c = copyWithBarcode(barcode);
        Lending l = new Lending();
        l.setBook(c);
        l.setLendingDate(start);
        l.setReturnDate(returnDate);
        l.setReturned(true);
        l.setDateReturned(dateReturned);
        l.setExtended(false);
        return l;
    }

    private Lending activeLending(String barcode, LocalDate start, LocalDate returnDate) {
        SchoolItemCopy c = copyWithBarcode(barcode);
        Lending l = new Lending();
        l.setBook(c);
        l.setLendingDate(start);
        l.setReturnDate(returnDate);
        l.setReturned(false);
        l.setDateReturned(null);
        l.setExtended(false);
        return l;
    }

    private SchoolItemCopy copyWithBarcode(String barcode) {
        SchoolItemCopy c = new SchoolItemCopy();
        c.setBarcode(barcode);
        c.setItem(item);
        return c;
    }

    private void stubMainRepositories(List<Lending> lendings, List<SchoolItemCopy> copies) {
        when(lendingRepository.findByBook_Item_IdAndSchool_Id(BOOK_ID, SCHOOL_ID)).thenReturn(lendings);
        when(copyRepository.findByItemIdAndSchoolId(BOOK_ID, SCHOOL_ID)).thenReturn(copies);
    }

    private void stubRankingRepositories(List<ItemCountDTO> lendingCounts, List<ItemCountDTO> copyCounts) {
        when(lendingRepository.countLendingsByItemForSchool(SCHOOL_ID)).thenReturn(lendingCounts);
        when(copyRepository.countCopiesByItemForSchool(SCHOOL_ID)).thenReturn(copyCounts);
    }

    // --- tests ---

    @Test
    void getBookStatistics_countsActiveLendingsOnly() {
        Lending active = activeLending("BC001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 20));
        Lending returned = completedLending("BC001", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 15), LocalDate.of(2024, 2, 14));
        stubMainRepositories(List.of(active, returned), List.of(copy));

        BookStatisticsDTO result = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6);

        assertEquals(2, result.totalLendings());
        assertEquals(1, result.activeLendings());
    }

    @Test
    void getBookStatistics_extensionPercent_roundsCorrectly() {
        Lending extended = completedLending("BC001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 15));
        extended.setExtended(true);
        Lending notExtended = completedLending("BC001", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 15), LocalDate.of(2024, 2, 15));
        stubMainRepositories(List.of(extended, notExtended), List.of(copy));

        BookStatisticsDTO result = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6);

        assertEquals(50, result.extensionPercent());
    }

    @Test
    void getBookStatistics_avgDuration_excludesActiveLendings() {
        // completed: Jan 1 → returned Jan 11 = 10 days
        Lending completed = completedLending("BC001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 11));
        // active lending must not affect the average
        Lending active = activeLending("BC001", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 28));
        stubMainRepositories(List.of(completed, active), List.of(copy));

        BookStatisticsDTO result = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6);

        assertEquals(10, result.avgDurationDays());
    }

    @Test
    void getBookStatistics_latePercent_countedWhenDateReturnedAfterReturnDate() {
        Lending late = completedLending("BC001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 15));
        Lending onTime = completedLending("BC001", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 8));
        stubMainRepositories(List.of(late, onTime), List.of(copy));

        BookStatisticsDTO result = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6);

        assertEquals(50, result.lateReturnPercent());
    }

    @Test
    void getBookStatistics_latePercent_activeOverdueLendingCountsAsLate() {
        // returnDate is well in the past → overdue
        Lending overdue = activeLending("BC001", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 10));
        // returnDate far in the future → not overdue
        Lending notOverdue = activeLending("BC001", LocalDate.of(2099, 1, 1), LocalDate.of(2099, 12, 31));
        stubMainRepositories(List.of(overdue, notOverdue), List.of(copy));

        BookStatisticsDTO result = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6);

        assertEquals(50, result.lateReturnPercent());
    }

    @Test
    void getBookStatistics_unavailableDays_detectsWhenAllCopiesOccupied() {
        SchoolItemCopy copy2 = new SchoolItemCopy();
        copy2.setBarcode("BC002");
        copy2.setItem(item);
        copy2.setCopy_status(CopyStatus.BESCHIKBAAR);

        // Both copies occupied simultaneously Jan 5–10 → 6 unavailable days
        Lending l1 = completedLending("BC001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 10));
        Lending l2 = completedLending("BC002", LocalDate.of(2024, 1, 5), LocalDate.of(2024, 1, 20), LocalDate.of(2024, 1, 15));
        stubMainRepositories(List.of(l1, l2), List.of(copy, copy2));

        BookStatisticsDTO result = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6);

        assertEquals(6, result.globalUnavailableDays());
    }

    @Test
    void getBookStatistics_monthlyTrend_trimmedToMostRecent6Months() {
        List<Lending> lendings = new ArrayList<>();
        for (int month = 6; month <= 12; month++) { // 7 distinct months
            lendings.add(completedLending("BC001",
                    LocalDate.of(2023, month, 1),
                    LocalDate.of(2023, month, 15),
                    LocalDate.of(2023, month, 15)));
        }
        stubMainRepositories(lendings, List.of(copy));

        List<BookStatisticsDTO.MonthlyLendingDTO> trend = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6).lendingsByMonth();

        assertEquals(6, trend.size());
        assertEquals("2023-07", trend.get(0).month()); // oldest kept month
        assertEquals("2023-12", trend.get(5).month()); // most recent
    }

    @Test
    void getBookStatistics_recentLendings_sortedByDateDescAndLimitedTo10() {
        List<Lending> lendings = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Lending l = completedLending("BC001",
                    LocalDate.of(2024, 1, i),
                    LocalDate.of(2024, 1, i + 5),
                    LocalDate.of(2024, 1, i + 5));
            l.setId((long) i);
            lendings.add(l);
        }
        stubMainRepositories(lendings, List.of(copy));

        List<BookStatisticsDTO.RecentLendingDTO> recent = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6).recentLendings();

        assertEquals(10, recent.size());
        assertEquals(12L, recent.get(0).id()); // most recent first
        assertEquals(3L, recent.get(9).id());  // oldest of the 10
    }

    @Test
    void getBookStatistics_campusStats_groupedByCampusAndSortedByLendingCount() {
        Campus campusA = new Campus();
        campusA.setName("Campus A");
        Campus campusB = new Campus();
        campusB.setName("Campus B");

        SchoolItemCopy copyA = new SchoolItemCopy();
        copyA.setBarcode("BC001");
        copyA.setItem(item);
        copyA.setCampus(campusA);
        copyA.setCopy_status(CopyStatus.BESCHIKBAAR);

        SchoolItemCopy copyB = new SchoolItemCopy();
        copyB.setBarcode("BC002");
        copyB.setItem(item);
        copyB.setCampus(campusB);
        copyB.setCopy_status(CopyStatus.UITGELEEND);

        Lending lA1 = completedLending("BC001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 10));
        Lending lA2 = completedLending("BC001", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 10));
        Lending lB  = completedLending("BC002", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 10), LocalDate.of(2024, 3, 10));
        stubMainRepositories(List.of(lA1, lA2, lB), List.of(copyA, copyB));

        List<BookStatisticsDTO.CampusStatisticsDTO> stats = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6).campusStats();

        assertEquals(2, stats.size());
        // highest lendingCount first
        BookStatisticsDTO.CampusStatisticsDTO a = stats.get(0);
        assertEquals("Campus A", a.name());
        assertEquals(2, a.lendingCount());
        assertEquals(1, a.totalCopies());
        assertEquals(1, a.availableCopies());
        assertEquals(2.0, a.lendingsPerCopy());

        BookStatisticsDTO.CampusStatisticsDTO b = stats.get(1);
        assertEquals("Campus B", b.name());
        assertEquals(1, b.lendingCount());
        assertEquals(0, b.availableCopies()); // UITGELEEND
    }

    @Test
    void getBookStatistics_campusStats_activeLendingsCountedSeparatelyFromCompleted() {
        Campus campus = new Campus();
        campus.setName("Campus A");

        SchoolItemCopy campusCopy = new SchoolItemCopy();
        campusCopy.setBarcode("BC001");
        campusCopy.setItem(item);
        campusCopy.setCampus(campus);
        campusCopy.setCopy_status(CopyStatus.UITGELEEND);

        Lending completed = completedLending("BC001", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 10));
        Lending active    = activeLending("BC001", LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 15));
        stubMainRepositories(List.of(completed, active), List.of(campusCopy));

        BookStatisticsDTO.CampusStatisticsDTO stat = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6).campusStats().get(0);

        assertEquals(2, stat.lendingCount());
        assertEquals(1, stat.activeLendings());
    }

    @Test
    void getBookStatistics_schoolRanking_withoutReadingLevel() {
        stubMainRepositories(List.of(), List.of(copy)); // item has no readingLevel

        stubRankingRepositories(
                List.of(new ItemCountDTO(BOOK_ID, 5L), new ItemCountDTO(1L, 10L)),
                List.of(new ItemCountDTO(BOOK_ID, 2L), new ItemCountDTO(1L, 1L))
        );

        BookStatisticsDTO.SchoolRankingDTO ranking = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6).schoolRanking();

        assertEquals(2, ranking.rankAbsolute());  // 5 < 10 → rank 2
        assertEquals(2, ranking.rankPerCopy());   // 2.5 < 10.0 → rank 2
        assertEquals(2, ranking.totalBooks());
        assertEquals(2.5, ranking.lendingsPerCopy());
        assertNull(ranking.rankReadingLevel());
        assertNull(ranking.totalBooksReadingLevel());
    }

    // --- class statistics tests ---

    private User studentUser(int id, String firstName, String lastName) {
        User u = mock(User.class);
        when(u.getId()).thenReturn(id);
        when(u.getFirstName()).thenReturn(firstName);
        when(u.getName()).thenReturn(lastName);
        when(u.getClasses()).thenReturn(List.of());
        return u;
    }

    private Lending lendingWithSnapshot(User borrower, String snapClassName, String snapSchoolYear,
                                        String snapReadingLevel, String snapBookGenre) {
        Lending l = new Lending();
        l.setBook(copy);
        l.setBorrower(borrower);
        l.setLendingDate(LocalDate.of(2025, 3, 1));
        l.setReturnDate(LocalDate.of(2025, 3, 15));
        l.setReturned(false);
        l.setSnapClassName(snapClassName);
        l.setSnapSchoolYear(snapSchoolYear);
        l.setSnapReadingLevel(snapReadingLevel);
        l.setSnapBookGenre(snapBookGenre);
        return l;
    }

    @Test
    void getClassStatistics_readingLevelDistribution_usesSnapshotNotLiveJoin() {
        String currentYear = SchoolYearUtil.determineSchoolYear(LocalDate.now());
        User student = studentUser(1, "Jan", "Peeters");

        SchoolClass sc = new SchoolClass("4A", null, List.of(student));
        sc.setId(10L);

        Lending l = lendingWithSnapshot(student, "4A", currentYear, "AVI-M6", "Fantasy");

        when(classRepository.findById(10L)).thenReturn(Optional.of(sc));
        when(lendingRepository.findBySnapClassNameAndSchool_Id("4A", SCHOOL_ID)).thenReturn(List.of(l));
        when(reviewRepository.countByAuthor_Id(1)).thenReturn(0L);

        ClassStatisticsDTO result = service.getClassStatistics(10L, SCHOOL_ID);

        assertEquals(Map.of("AVI-M6", 1), result.readingLevelDistribution());
    }

    @Test
    void getClassStatistics_readingLevelDistribution_excludesPreviousSchoolYear() {
        String currentYear = SchoolYearUtil.determineSchoolYear(LocalDate.now());
        String previousYear = (Integer.parseInt(currentYear.substring(0, 4)) - 1)
                + "-" + currentYear.substring(0, 4);
        User student = studentUser(1, "Jan", "Peeters");

        SchoolClass sc = new SchoolClass("4A", null, List.of(student));
        sc.setId(10L);

        Lending currentYearLending = lendingWithSnapshot(student, "4A", currentYear, "AVI-M6", null);
        Lending oldLending = lendingWithSnapshot(student, "4A", previousYear, "AVI-E3", null);

        when(classRepository.findById(10L)).thenReturn(Optional.of(sc));
        when(lendingRepository.findBySnapClassNameAndSchool_Id("4A", SCHOOL_ID))
                .thenReturn(List.of(currentYearLending, oldLending));
        when(reviewRepository.countByAuthor_Id(1)).thenReturn(0L);

        ClassStatisticsDTO result = service.getClassStatistics(10L, SCHOOL_ID);

        assertEquals(Map.of("AVI-M6", 1), result.readingLevelDistribution());
        assertFalse(result.readingLevelDistribution().containsKey("AVI-E3"));
    }

    @Test
    void getClassStatistics_classSwitchAfterLending_historicalLendingStillCounted() {
        String currentYear = SchoolYearUtil.determineSchoolYear(LocalDate.now());
        User movedStudent = studentUser(2, "Els", "Janssen");

        // Class 4A now has no students (movedStudent switched to 4B)
        SchoolClass sc = new SchoolClass("4A", null, List.of());
        sc.setId(10L);

        // But the lending snapshot still carries "4A"
        Lending l = lendingWithSnapshot(movedStudent, "4A", currentYear, null, null);

        when(classRepository.findById(10L)).thenReturn(Optional.of(sc));
        when(lendingRepository.findBySnapClassNameAndSchool_Id("4A", SCHOOL_ID)).thenReturn(List.of(l));

        ClassStatisticsDTO result = service.getClassStatistics(10L, SCHOOL_ID);

        assertEquals(1, result.totalLendings());
    }

    @Test
    void getBookStatistics_schoolRanking_withReadingLevel() {
        ReadingLevel level = new ReadingLevel("Niveau 3", "Beschrijving");
        level.setId(5L);
        item.setReadingLevel(level);

        stubMainRepositories(List.of(), List.of(copy));
        stubRankingRepositories(
                List.of(new ItemCountDTO(BOOK_ID, 3L)),
                List.of(new ItemCountDTO(BOOK_ID, 1L))
        );
        when(copyRepository.findItemIdsBySchoolAndReadingLevel(SCHOOL_ID, 5L))
                .thenReturn(List.of(BOOK_ID, 99L));
        when(lendingRepository.countLendingsByItemForSchoolAndReadingLevel(SCHOOL_ID, 5L))
                .thenReturn(List.of(new ItemCountDTO(BOOK_ID, 3L), new ItemCountDTO(99L, 7L)));

        BookStatisticsDTO.SchoolRankingDTO ranking = service.getBookStatistics(BOOK_ID, SCHOOL_ID, "month", 6).schoolRanking();

        assertEquals(2, ranking.rankReadingLevel()); // 3 < 7 → rank 2 within level
        assertEquals(2, ranking.totalBooksReadingLevel());
    }
}
