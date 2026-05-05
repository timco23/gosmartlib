package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.BookImportRow;
import edu.ap.spring_backend.dto.CsvPreviewDTO;
import edu.ap.spring_backend.dto.RowError;
import edu.ap.spring_backend.jpa.Campus;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.School;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookCsvImportServiceTest {

    private static final String HEADER =
            "Titel;Auteur;ISBN;Conditie_nieuw;Conditie_goed;Conditie_gebruikt;Conditie_beschadigd;Campus";
    private static final Long SCHOOL_ID = 1L;

    @Mock private ItemRepository itemRepository;
    @Mock private SchoolItemCopyRepository schoolItemCopyRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BookCsvImportService service;

    private School school;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        school = new School();
        school.setId(SCHOOL_ID);
        school.setName("Test School");
    }

    private MockMultipartFile csv(String... rows) {
        String content = HEADER + "\n" + String.join("\n", rows);
        return new MockMultipartFile("file", "books.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

    private void mockSchool(String... campusNames) {
        List<Campus> campuses = new ArrayList<>();
        int id = 1;
        for (String name : campusNames) {
            Campus c = new Campus();
            c.setId((long) id++);
            c.setName(name);
            campuses.add(c);
        }
        school.setCampuses(campuses);
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(school));
    }

    private Item stubItemWithIsbn(String title, String isbn) {
        Item item = new Item();
        item.setTitle(title);
        item.setIsbn(isbn);
        return item;
    }

    private static int rowNumber(RowError error) {
        try {
            return (int) error.getClass().getMethod("rowNumber").invoke(error);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String message(RowError error) {
        try {
            return (String) error.getClass().getMethod("message").invoke(error);
        } catch (Exception ex) {
            return error.toString();
        }
    }

    private static boolean hasError(List<RowError> errors, int row, String contains) {
        return errors.stream().anyMatch(e -> rowNumber(e) == row && message(e).contains(contains));
    }

    // previewImport — file validation

    @Test
    void givenFileLargerThan1MB_whenPreviewImport_thenReturnsError() {
        byte[] big = new byte[1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "books.csv", "text/csv", big);

        CsvPreviewDTO result = service.previewImport(file, SCHOOL_ID);

        assertEquals(1, result.criticalErrors().size());
        assertTrue(result.criticalErrors().get(0).contains("Bestand is te groot"));
        assertTrue(result.errors().isEmpty());
        assertTrue(result.rows().isEmpty());
    }

    @Test
    void givenNonCsvExtension_whenPreviewImport_thenReturnsError() {
        MockMultipartFile file = new MockMultipartFile("file", "books.txt", "text/plain", "x".getBytes());

        CsvPreviewDTO result = service.previewImport(file, SCHOOL_ID);

        assertEquals(1, result.criticalErrors().size());
        assertTrue(result.criticalErrors().get(0).contains("Ongeldig bestandstype"));
    }

    @Test
    void givenNoFilename_whenPreviewImport_thenReturnsError() {
        MockMultipartFile file = new MockMultipartFile("file", null, "text/csv", "x".getBytes());

        CsvPreviewDTO result = service.previewImport(file, SCHOOL_ID);

        assertEquals(1, result.criticalErrors().size());
        assertTrue(result.criticalErrors().get(0).contains("Ongeldig bestandstype"));
    }

    @Test
    void givenTooManyRows_whenPreviewImport_thenReturnsError() {
        mockSchool("Main Campus");

        String row = "Dune;Frank;9780441568529;1;0;0;0;Main Campus";
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < 1001; i++) rows.add(row);

        CsvPreviewDTO result = service.previewImport(csv(rows.toArray(new String[0])), SCHOOL_ID);

        assertEquals(1, result.criticalErrors().size());
        assertTrue(result.criticalErrors().get(0).contains("Bestand bevat meer dan 1000 rijen"));
    }

    // previewImport — success cases

    @Test
    void givenValidRow_whenPreviewImport_thenRowParsedCorrectly() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank Herbert;9780441568529;1;2;3;4;Main Campus"
        ), SCHOOL_ID);

        assertTrue(result.criticalErrors().isEmpty());
        assertTrue(result.errors().isEmpty());
        assertEquals(1, result.rows().size());

        BookImportRow row = result.rows().get(0);
        assertEquals(2, row.rowNumber());
        assertEquals("Dune", row.title());
        assertEquals("Frank Herbert", row.author());
        assertEquals("9780441568529", row.isbn());
        assertEquals(1, row.conditionNieuw());
        assertEquals(2, row.conditionGoed());
        assertEquals(3, row.conditionGebruikt());
        assertEquals(4, row.conditionBeschadigd());
        assertEquals("Main Campus", row.campus());
    }

    @Test
    void givenAllConditionsZero_whenPreviewImport_thenReturnsError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;0;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(hasError(result.errors(), 2, "totaal aantal exemplaren"));
    }

    @Test
    void givenValidIsbn13_whenPreviewImport_thenNoIsbnError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(result.errors().stream().noneMatch(e -> message(e).contains("ISBN")));
    }

    @Test
    void givenEmptyIsbn_whenPreviewImport_thenNoIsbnError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(result.errors().stream().noneMatch(e -> message(e).contains("ISBN")));
    }

    @Test
    void givenValidConditions_whenPreviewImport_thenNoError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(result.errors().isEmpty());
    }

    @Test
    void givenConditionColumns_whenPreviewImport_thenConditionsParsedCorrectly() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;2;3;4;Main Campus"
        ), SCHOOL_ID);

        BookImportRow row = result.rows().get(0);
        assertEquals(1, row.conditionNieuw());
        assertEquals(2, row.conditionGoed());
        assertEquals(3, row.conditionGebruikt());
        assertEquals(4, row.conditionBeschadigd());
    }

    @Test
    void givenMultipleValidRows_whenPreviewImport_thenAllRowsReturned() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;0;0;0;Main Campus",
                "Neuromancer;Gibson;9780441569595;0;1;0;0;Main Campus"
        ), SCHOOL_ID);

        assertEquals(2, result.rows().size());
    }

    @Test
    void givenMultipleRows_whenPreviewImport_thenRowNumbersStartAtTwo() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;0;0;0;Main Campus",
                "Neuromancer;Gibson;9780441569595;0;1;0;0;Main Campus"
        ), SCHOOL_ID);

        assertEquals(2, result.rows().get(0).rowNumber());
        assertEquals(3, result.rows().get(1).rowNumber());
    }

    @Test
    void givenKnownCampus_whenPreviewImport_thenNoError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(result.errors().isEmpty());
    }

    @Test
    void givenCampusWithDifferentCaseAndSpaces_whenPreviewImport_thenNormalizedAndMatched() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;0;0;0;  main   campus  "
        ), SCHOOL_ID);

        assertTrue(result.errors().isEmpty());
    }

    @Test
    void givenSameTitleOnDifferentCampuses_whenPreviewImport_thenBothAllowed() {
        mockSchool("Campus A", "Campus B");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;0;0;0;Campus A",
                "Dune;Frank;9780441568529;1;0;0;0;Campus B"
        ), SCHOOL_ID);

        assertTrue(result.errors().stream().noneMatch(e -> message(e).contains("titel + campus komt meerdere keren")));
    }

    // previewImport — ISBN conflict detection

    @Test
    void givenIsbnConflict_whenPreviewImport_thenExistingIsbnPopulatedOnRow() {
        mockSchool("Main Campus");
        when(itemRepository.findByTitle("Dune")).thenReturn(Optional.of(stubItemWithIsbn("Dune", "1111111111111")));

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;2222222222222;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertEquals("1111111111111", result.rows().get(0).existingIsbn());
    }

    @Test
    void givenNoIsbnConflict_whenPreviewImport_thenExistingIsbnEmpty() {
        mockSchool("Main Campus");
        when(itemRepository.findByTitle("Dune")).thenReturn(Optional.of(stubItemWithIsbn("Dune", "9780441568529")));

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(result.rows().get(0).existingIsbn().isEmpty());
    }

    @Test
    void givenExistingItemWithEmptyIsbn_whenPreviewImport_thenNoIsbnConflict() {
        mockSchool("Main Campus");
        when(itemRepository.findByTitle("Dune")).thenReturn(Optional.of(stubItemWithIsbn("Dune", "")));

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(result.rows().get(0).existingIsbn().isEmpty());
    }

    @Test
    void givenNoIsbnInCsv_whenPreviewImport_thenNoIsbnConflictCheck() {
        mockSchool("Main Campus");

        service.previewImport(csv(
                "Dune;Frank;;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        verify(itemRepository, never()).findByTitle("Dune");
    }

    // previewImport — error cases

    @Test
    void givenMissingTitle_whenPreviewImport_thenReturnsError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                ";Frank;9780441568529;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(hasError(result.errors(), 2, "'Titel' is verplicht"));
    }

    @Test
    void givenMissingAuthor_whenPreviewImport_thenReturnsError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;;9780441568529;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(hasError(result.errors(), 2, "'Auteur' is verplicht"));
    }

    @Test
    void givenMissingTitleAndAuthor_whenPreviewImport_thenReturnsBothErrors() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                ";;9780441568529;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(hasError(result.errors(), 2, "'Titel' is verplicht"));
        assertTrue(hasError(result.errors(), 2, "'Auteur' is verplicht"));
    }

    @Test
    void givenIsbnTooShort_whenPreviewImport_thenReturnsError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;123;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(hasError(result.errors(), 2, "'ISBN' moet exact 13 cijfers bevatten"));
    }

    @Test
    void givenIsbnWithLetters_whenPreviewImport_thenReturnsError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;ABCDEF1234567;1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(hasError(result.errors(), 2, "'ISBN' moet exact 13 cijfers bevatten"));
    }

    @Test
    void givenNegativeCondition_whenPreviewImport_thenReturnsError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;-1;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(hasError(result.errors(), 2, "'Conditie_nieuw' mag niet negatief zijn"));
    }

    @Test
    void givenConditionNotANumber_whenPreviewImport_thenReturnsError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;x;0;0;0;Main Campus"
        ), SCHOOL_ID);

        assertTrue(hasError(result.errors(), 2, "'Conditie_nieuw' moet een getal zijn"));
    }

    @Test
    void givenAllConditionColumnsInvalid_whenPreviewImport_thenAllFourErrorsReturned() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;x;y;z;w;Main Campus"
        ), SCHOOL_ID);

        long count = result.errors().stream()
                .map(BookCsvImportServiceTest::message)
                .filter(m -> m.contains("moet een getal zijn"))
                .count();

        assertTrue(count >= 4);
    }

    @Test
    void givenUnknownCampus_whenPreviewImport_thenReturnsError() {
        mockSchool("Main Campus");

        CsvPreviewDTO result = service.previewImport(csv(
                "Dune;Frank;9780441568529;1;0;0;0;Unknown"
        ), SCHOOL_ID);

        assertTrue(hasError(result.errors(), 2, "campus 'Unknown' niet gevonden"));
    }
}