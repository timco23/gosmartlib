package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.*;
import edu.ap.spring_backend.service.bookmetadata.BookMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookCsvImportServiceTest {

    private static final String HEADER =
            "Titel;Auteur;ISBN;Conditie_nieuw;Conditie_goed;Conditie_gebruikt;Conditie_beschadigd;Campus";

    @Mock private ItemRepository itemRepository;
    @Mock private SchoolItemCopyRepository schoolItemCopyRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private BookMetadataService bookMetadataService;

    @InjectMocks
    private BookCsvImportService service;

    private School school;
    private Campus campus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        school = new School();
        school.setId(1L);
        school.setName("Test School");
        school.setCampuses(new ArrayList<>());

        campus = new Campus();
        campus.setId(1L);
        campus.setName("Ellermanstraat");
        campus.setSchool(school);
    }

    // helpers

    private MockMultipartFile csv(String... lines) {
        String content = String.join("\n", lines);
        return new MockMultipartFile("file", "import.csv", "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
    }

    private void mockSchool() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
    }

    private Item stubItem(String title) {
        return new Item("desc", 0, 0.0, "", 0, "NEDERLANDS", 0, "", "Auteur", "Publisher",
                title, new HashSet<>());
    }

    private Item stubItemWithIsbn(String title, String isbn) {
        return new Item("desc", 0, 0.0, "", 0, "NEDERLANDS", 0, isbn, "Auteur", "Publisher",
                title, new HashSet<>());
    }

    // previewImport — file validation

    @Test
    void givenFileLargerThan1MB_whenPreviewImport_thenReturnsError() {
        byte[] largeContent = new byte[1024 * 1024 + 1];
        MockMultipartFile largeFile = new MockMultipartFile("file", "import.csv", "text/csv", largeContent);

        CsvPreviewDTO result = service.previewImport(largeFile, 1L);

        assertFalse(result.criticalErrors().isEmpty());
        assertTrue(result.criticalErrors().get(0).contains("te groot"));
    }

    @Test
    void givenNonCsvExtension_whenPreviewImport_thenReturnsError() {
        MockMultipartFile txtFile = new MockMultipartFile("file", "import.txt", "text/plain",
                (HEADER + "\nBoek;Auteur;;1;0;0;0;").getBytes(StandardCharsets.UTF_8));

        CsvPreviewDTO result = service.previewImport(txtFile, 1L);

        assertFalse(result.criticalErrors().isEmpty());
        assertTrue(result.criticalErrors().get(0).contains("Ongeldig bestandstype"));
    }

    @Test
    void givenNoFilename_whenPreviewImport_thenReturnsError() {
        MockMultipartFile noNameFile = new MockMultipartFile("file", null, "text/csv",
                (HEADER + "\nBoek;Auteur;;1;0;0;0;").getBytes(StandardCharsets.UTF_8));

        CsvPreviewDTO result = service.previewImport(noNameFile, 1L);

        assertFalse(result.criticalErrors().isEmpty());
        assertTrue(result.criticalErrors().get(0).contains("Ongeldig bestandstype"));
    }

    @Test
    void givenTooManyRows_whenPreviewImport_thenReturnsError() {
        mockSchool();
        when(itemRepository.findByTitle(any())).thenReturn(Optional.empty());

        String[] lines = new String[1002]; // header + 1001 data rows
        lines[0] = HEADER;
        for (int i = 1; i <= 1001; i++) {
            lines[i] = "Boek " + i + ";Auteur;;1;0;0;0;";
        }

        CsvPreviewDTO result = service.previewImport(csv(lines), 1L);

        assertTrue(result.criticalErrors().stream().anyMatch(e -> e.contains("meer dan")));
    }

    // previewImport — success cases

    @Test
    void givenValidRow_whenPreviewImport_thenRowParsedCorrectly() {
        mockSchool();
        when(itemRepository.findByTitle("De Kleine Prins")).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "De Kleine Prins;Saint-Exupéry;;1;0;0;0;"), 1L);

        assertTrue(result.errors().isEmpty());
        assertEquals(1, result.rows().size());
        BookImportRow row = result.rows().get(0);
        assertEquals("De Kleine Prins", row.title());
        assertEquals("Saint-Exupéry", row.author());
        assertEquals("", row.existingIsbn());
    }

    @Test
    void givenAllConditionsZero_whenPreviewImport_thenReturnsError() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;0;0;0;0;"), 1L);

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("totaal aantal exemplaren")));
    }

    @Test
    void givenValidIsbn13_whenPreviewImport_thenNoIsbnError() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;9780140328721;1;0;0;0;"), 1L);

        assertTrue(result.errors().isEmpty());
        assertEquals("9780140328721", result.rows().get(0).isbn());
    }

    @Test
    void givenEmptyIsbn_whenPreviewImport_thenNoIsbnError() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;"), 1L);

        assertTrue(result.errors().stream().noneMatch(e -> e.message().contains("ISBN")));
    }

    @Test
    void givenValidConditions_whenPreviewImport_thenNoError() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;1;1;1;"), 1L);

        assertTrue(result.errors().isEmpty());
    }

    @Test
    void givenConditionColumns_whenPreviewImport_thenConditionsParsedCorrectly() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;2;1;1;"), 1L);

        assertTrue(result.errors().isEmpty());
        BookImportRow row = result.rows().get(0);
        assertEquals(1, row.conditionNieuw());
        assertEquals(2, row.conditionGoed());
        assertEquals(1, row.conditionGebruikt());
        assertEquals(1, row.conditionBeschadigd());
    }

    @Test
    void givenMultipleValidRows_whenPreviewImport_thenAllRowsReturned() {
        mockSchool();
        when(itemRepository.findByTitle(any())).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "Boek Een;Auteur A;;1;1;0;0;",
                "Boek Twee;Auteur B;;1;0;0;0;"),
                1L);

        assertTrue(result.errors().isEmpty());
        assertEquals(2, result.rows().size());
        assertEquals("Boek Een", result.rows().get(0).title());
        assertEquals("Boek Twee", result.rows().get(1).title());
    }

    @Test
    void givenMultipleRows_whenPreviewImport_thenRowNumbersStartAtTwo() {
        mockSchool();
        when(itemRepository.findByTitle(any())).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "Boek A;Auteur;;1;0;0;0;",
                "Boek B;Auteur;;1;0;0;0;"),
                1L);

        assertEquals(2, result.rows().get(0).rowNumber());
        assertEquals(3, result.rows().get(1).rowNumber());
    }

    @Test
    void givenKnownCampus_whenPreviewImport_thenNoError() {
        mockSchool();
        school.getCampuses().add(campus);
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;Ellermanstraat"), 1L);

        assertTrue(result.errors().isEmpty());
    }

    @Test
    void givenCampusWithDifferentCaseAndSpaces_whenPreviewImport_thenNormalizedAndMatched() {
        mockSchool();
        Campus spaced = new Campus();
        spaced.setName("Ellerman Straat");
        school.getCampuses().add(spaced);
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;Ellerman Straat"), 1L);

        assertTrue(result.errors().isEmpty());
    }

    @Test
    void givenSameTitleOnDifferentCampuses_whenPreviewImport_thenBothAllowed() {
        mockSchool();
        Campus campusA = new Campus(); campusA.setName("Campus A");
        Campus campusB = new Campus(); campusB.setName("Campus B");
        school.getCampuses().add(campusA);
        school.getCampuses().add(campusB);
        when(itemRepository.findByTitle(any())).thenReturn(Optional.empty());

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "Boek;Auteur;;1;0;0;0;Campus A",
                "Boek;Auteur;;1;0;0;0;Campus B"),
                1L);

        assertTrue(result.errors().isEmpty());
        assertEquals(2, result.rows().size());
    }

    // previewImport — ISBN conflict detection

    @Test
    void givenIsbnConflict_whenPreviewImport_thenExistingIsbnPopulatedOnRow() {
        mockSchool();
        when(itemRepository.findByTitle("Bestaand Boek"))
                .thenReturn(Optional.of(stubItemWithIsbn("Bestaand Boek", "9780000000001")));

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Bestaand Boek;Auteur;9780000000002;1;0;0;0;"), 1L);

        assertTrue(result.errors().isEmpty());
        BookImportRow row = result.rows().get(0);
        assertEquals("9780000000001", row.existingIsbn());
        assertFalse(row.updateExistingIsbn());
    }

    @Test
    void givenNoIsbnConflict_whenPreviewImport_thenExistingIsbnEmpty() {
        mockSchool();
        when(itemRepository.findByTitle("Boek"))
                .thenReturn(Optional.of(stubItemWithIsbn("Boek", "9780140328721")));

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;9780140328721;1;0;0;0;"), 1L);

        assertTrue(result.errors().isEmpty());
        assertEquals("", result.rows().get(0).existingIsbn());
    }

    @Test
    void givenExistingItemWithEmptyIsbn_whenPreviewImport_thenNoIsbnConflict() {
        mockSchool();
        when(itemRepository.findByTitle("Boek"))
                .thenReturn(Optional.of(stubItemWithIsbn("Boek", "")));

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;9780140328721;1;0;0;0;"), 1L);

        assertTrue(result.errors().isEmpty());
        assertEquals("", result.rows().get(0).existingIsbn());
    }

    @Test
    void givenNoIsbnInCsv_whenPreviewImport_thenNoIsbnConflictCheck() {
        mockSchool();
        when(itemRepository.findByTitle("Boek"))
                .thenReturn(Optional.of(stubItemWithIsbn("Boek", "9780000000001")));

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;"), 1L);

        assertTrue(result.errors().isEmpty());
        assertEquals("", result.rows().get(0).existingIsbn());
    }

    // previewImport — error cases

    @Test
    void givenMissingTitle_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, ";Auteur;;1;0;0;0;"), 1L);

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("'Titel' is verplicht")));
    }

    @Test
    void givenMissingAuthor_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;;;1;0;0;0;"), 1L);

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("'Auteur' is verplicht")));
    }

    @Test
    void givenMissingTitleAndAuthor_whenPreviewImport_thenReturnsBothErrors() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, ";;;1;0;0;0;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("'Titel' is verplicht")));
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("'Auteur' is verplicht")));
    }

    @Test
    void givenIsbnTooShort_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;123;1;0;0;0;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("'ISBN' moet exact 13 cijfers bevatten")));
    }

    @Test
    void givenIsbnWithLetters_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;978ABC3456789;1;0;0;0;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("'ISBN' moet exact 13 cijfers bevatten")));
    }

    @Test
    void givenNegativeCondition_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;-1;0;0;0;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("mag niet negatief zijn")));
    }

    @Test
    void givenConditionNotANumber_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;abc;0;0;0;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("'Conditie_nieuw' moet een getal zijn")));
    }

    @Test
    void givenAllConditionColumnsInvalid_whenPreviewImport_thenAllFourErrorsReturned() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;x;y;z;w;"), 1L);

        assertEquals(4, result.errors().stream()
                .filter(e -> e.message().contains("moet een getal zijn")).count());
    }

    @Test
    void givenUnknownCampus_whenPreviewImport_thenReturnsError() {
        mockSchool();
        school.getCampuses().add(campus);

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;onbekendcampus"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("niet gevonden voor deze school")));
    }

    @Test
    void givenDuplicateTitleSameCampus_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "Boek;Auteur;;1;0;0;0;",
                "Boek;Auteur;;1;0;0;0;"),
                1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("titel + campus komt meerdere keren voor")));
    }

    @Test
    void givenDuplicateTitleDifferentCase_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "boek;Auteur;;1;0;0;0;",
                "BOEK;Auteur;;1;0;0;0;"),
                1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("titel + campus komt meerdere keren voor")));
    }

    @Test
    void givenWrongColumnCount_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;te;veel;kolom;men;hier;echt;te;veel"), 1L);

        assertTrue(result.criticalErrors().stream().anyMatch(e -> e.contains("incorrecte hoeveelheid kolommen")));
    }

    @Test
    void givenRowWithWrongColumnCount_whenPreviewImport_thenCriticalErrorReturnedAndRowsEmpty() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "Boek;Auteur;te;veel;kolommen;hier;echt;te;veel;extra",
                ";Auteur;;1;0;0;0;"),
                1L);

        assertTrue(result.criticalErrors().stream().anyMatch(e -> e.contains("incorrecte hoeveelheid kolommen")));
        assertTrue(result.rows().isEmpty());
    }

    @Test
    void givenMultipleErrorsOnSameRow_whenPreviewImport_thenAllErrorsCollected() {
        mockSchool();

        // Missing title, missing author, negative condition, conditionSum < 1
        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, ";;;-1;0;0;0;"), 1L);

        assertTrue(result.errors().size() >= 3);
    }

    @Test
    void givenUnknownSchoolId_whenPreviewImport_thenThrowsEntityNotFoundException() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.previewImport(csv(HEADER, "Boek;Auteur;;1;0;0;0;"), 99L));
    }

    @Test
    void givenValidationErrors_whenPreviewImport_thenRowStillReturnedForEnrichment() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, ";Auteur;;1;0;0;0;"), 1L);

        assertFalse(result.errors().isEmpty());
        assertFalse(result.rows().isEmpty());
    }

    // confirmImport

    @Test
    void givenNewItem_whenConfirmImport_thenItemSavedAndCopyCreated() {
        mockSchool();
        when(itemRepository.findByTitle("Nieuw Boek")).thenReturn(Optional.empty());
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Nieuw Boek", "Auteur", "", 1, 0, 0, 0, "", "", false)));

        CsvImportDTO result = service.confirmImport(dto);

        assertTrue(result.errors().isEmpty());
        assertEquals(1, result.importedBooks());
        assertEquals(1, result.importedCopies());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void givenExistingItem_whenConfirmImport_thenItemNotSavedAgain() {
        mockSchool();
        when(itemRepository.findByTitle("Bestaand Boek")).thenReturn(Optional.of(stubItem("Bestaand Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Bestaand Boek", "Auteur", "", 1, 0, 0, 0, "", "", false)));

        service.confirmImport(dto);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void givenIsbnConflictWithUpdateTrue_whenConfirmImport_thenExistingItemIsbnUpdated() {
        mockSchool();
        Item existingItem = stubItemWithIsbn("Boek", "9780000000001");
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "9780000000002", 0, 0, 0, 1, "", "9780000000001", true)));

        service.confirmImport(dto);

        assertEquals("9780000000002", existingItem.getIsbn());
        verify(itemRepository).save(existingItem);
    }

    @Test
    void givenIsbnConflictWithUpdateFalse_whenConfirmImport_thenIsbnNotUpdated() {
        mockSchool();
        Item existingItem = stubItemWithIsbn("Boek", "9780000000001");
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(existingItem));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "9780000000002", 0, 0, 0, 1, "", "9780000000001", false)));

        service.confirmImport(dto);

        assertEquals("9780000000001", existingItem.getIsbn());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void givenConditionDistribution_whenConfirmImport_thenCopiesCreatedWithCorrectConditions() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(stubItem("Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // 4 copies: 1 NIEUW, 1 GOED, 1 GEBRUIKT, 1 BESCHADIGD
        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 1, 1, 1, 1, "", "", false)));

        service.confirmImport(dto);

        ArgumentCaptor<SchoolItemCopy> captor = ArgumentCaptor.forClass(SchoolItemCopy.class);
        verify(schoolItemCopyRepository, atLeastOnce()).save(captor.capture());

        List<ItemCondition> conditions = captor.getAllValues().stream()
                .map(SchoolItemCopy::getCondition).distinct().toList();
        assertTrue(conditions.contains(ItemCondition.NIEUW));
        assertTrue(conditions.contains(ItemCondition.GOED));
        assertTrue(conditions.contains(ItemCondition.GEBRUIKT));
        assertTrue(conditions.contains(ItemCondition.BESCHADIGD));
        assertFalse(conditions.contains(ItemCondition.ONGEDEFINIEERD));
    }

    @Test
    void givenAllConditionsZero_whenConfirmImport_thenReturnsErrors() {
        mockSchool();

        CsvImportDTO result = service.confirmImport(new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 0, 0, 0, 0, "", "", false))));

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("totaal aantal exemplaren")));
        verifyNoInteractions(itemRepository, schoolItemCopyRepository);
    }

    @Test
    void givenKnownCampus_whenConfirmImport_thenCampusResolvedOnCopy() {
        mockSchool();
        school.getCampuses().add(campus);
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(stubItem("Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 1, 0, 0, 0, "ellermanstraat", "", false)));

        service.confirmImport(dto);

        ArgumentCaptor<SchoolItemCopy> captor = ArgumentCaptor.forClass(SchoolItemCopy.class);
        verify(schoolItemCopyRepository, atLeastOnce()).save(captor.capture());
        assertEquals(campus, captor.getValue().getCampus());
    }

    @Test
    void givenNoCampusMatch_whenConfirmImport_thenNullCampusOnCopy() {
        mockSchool();
        school.getCampuses().add(campus);
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(stubItem("Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 1, 0, 0, 0, "", "", false)));

        service.confirmImport(dto);

        ArgumentCaptor<SchoolItemCopy> captor = ArgumentCaptor.forClass(SchoolItemCopy.class);
        verify(schoolItemCopyRepository, atLeastOnce()).save(captor.capture());
        assertNull(captor.getValue().getCampus());
    }

    @Test
    void givenMultipleRows_whenConfirmImport_thenCountsSummedCorrectly() {
        mockSchool();
        when(itemRepository.findByTitle("Boek A")).thenReturn(Optional.of(stubItem("Boek A")));
        when(itemRepository.findByTitle("Boek B")).thenReturn(Optional.of(stubItem("Boek B")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek A", "A1", "", 2, 1, 0, 0, "", "", false),
                new BookImportRow(3, "Boek B", "A2", "", 0, 2, 0, 0, "", "", false)));

        CsvImportDTO result = service.confirmImport(dto);

        assertEquals(2, result.importedBooks());
        assertEquals(5, result.importedCopies());
    }

    @Test
    void givenImportedCopies_whenConfirmImport_thenAllCopiesHaveBeschikbaarStatus() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(stubItem("Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 1, 1, 0, 0, "", "", false)));

        service.confirmImport(dto);

        ArgumentCaptor<SchoolItemCopy> captor = ArgumentCaptor.forClass(SchoolItemCopy.class);
        verify(schoolItemCopyRepository, atLeastOnce()).save(captor.capture());
        assertTrue(captor.getAllValues().stream()
                .allMatch(c -> c.getCopy_status() == CopyStatus.BESCHIKBAAR));
    }

    @Test
    void givenNewItem_whenConfirmImport_thenItemCreatedWithDefaultLanguage() {
        mockSchool();
        when(itemRepository.findByTitle("Mijn Boek")).thenReturn(Optional.empty());
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Mijn Boek", "Schrijver", "", 1, 0, 0, 0, "", "", false)));

        service.confirmImport(dto);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        assertEquals("Mijn Boek", captor.getValue().getTitle());
        assertEquals("Schrijver", captor.getValue().getAuthor());
        assertEquals("NEDERLANDS", captor.getValue().getBookLanguage());
    }

    @Test
    void givenEmptyRowList_whenConfirmImport_thenReturnsZeroCounts() {
        mockSchool();

        CsvImportDTO result = service.confirmImport(new CsvConfirmDTO(1L, List.of()));

        assertEquals(0, result.importedBooks());
        assertEquals(0, result.importedCopies());
        verifyNoInteractions(itemRepository, schoolItemCopyRepository);
    }

    @Test
    void givenUnknownSchoolId_whenConfirmImport_thenThrowsEntityNotFoundException() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.confirmImport(new CsvConfirmDTO(99L, List.of())));
    }

    // confirmImport — re-validation

    @Test
    void givenMissingTitle_whenConfirmImport_thenReturnsErrors() {
        mockSchool();

        CsvImportDTO result = service.confirmImport(new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "", "Auteur", "", 1, 0, 0, 0, "", "", false))));

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("'Titel' is verplicht")));
        verifyNoInteractions(itemRepository, schoolItemCopyRepository);
    }

    @Test
    void givenMissingAuthor_whenConfirmImport_thenReturnsErrors() {
        mockSchool();

        CsvImportDTO result = service.confirmImport(new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "", "", 1, 0, 0, 0, "", "", false))));

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("'Auteur' is verplicht")));
        verifyNoInteractions(itemRepository, schoolItemCopyRepository);
    }

    @Test
    void givenInvalidIsbn_whenConfirmImport_thenReturnsErrors() {
        mockSchool();

        CsvImportDTO result = service.confirmImport(new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "123", 1, 0, 0, 0, "", "", false))));

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("'ISBN' moet exact 13 cijfers bevatten")));
        verifyNoInteractions(itemRepository, schoolItemCopyRepository);
    }

    @Test
    void givenNegativeCondition_whenConfirmImport_thenReturnsErrors() {
        mockSchool();

        CsvImportDTO result = service.confirmImport(new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", -1, 0, 0, 0, "", "", false))));

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("mag niet negatief zijn")));
        verifyNoInteractions(itemRepository, schoolItemCopyRepository);
    }

    @Test
    void givenUnknownCampus_whenConfirmImport_thenReturnsErrors() {
        mockSchool();
        school.getCampuses().add(campus);

        CsvImportDTO result = service.confirmImport(new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 1, 0, 0, 0, "onbekendcampus", "", false))));

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("niet gevonden voor deze school")));
        verifyNoInteractions(itemRepository, schoolItemCopyRepository);
    }

    @Test
    void givenDuplicateTitleSameCampus_whenConfirmImport_thenReturnsErrors() {
        mockSchool();

        CsvImportDTO result = service.confirmImport(new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 1, 0, 0, 0, "", "", false),
                new BookImportRow(3, "Boek", "Auteur", "", 1, 0, 0, 0, "", "", false))));

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("titel + campus komt meerdere keren voor")));
        verifyNoInteractions(itemRepository, schoolItemCopyRepository);
    }

    @Test
    void givenTooManyRows_whenConfirmImport_thenReturnsError() {
        mockSchool();

        List<BookImportRow> rows = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            rows.add(new BookImportRow(i + 2, "Boek " + i, "Auteur", "", 1, 0, 0, 0, "", "", false));
        }

        CsvImportDTO result = service.confirmImport(new CsvConfirmDTO(1L, rows));

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().get(0).message().contains("Te veel rijen"));
        verifyNoInteractions(itemRepository, schoolItemCopyRepository);
    }

    @Test
    void givenValidationErrors_whenConfirmImport_thenNothingWrittenToDb() {
        mockSchool();

        service.confirmImport(new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "", "", "bad-isbn", -1, 0, 0, 0, "", "", false))));

        verifyNoInteractions(itemRepository, schoolItemCopyRepository);
    }

    @Test
    void givenPartialConditions_whenConfirmImport_thenExactCopyCountSaved() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(stubItem("Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // 3 copies: 2 NIEUW + 1 GOED
        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 2, 1, 0, 0, "", "", false)));

        CsvImportDTO result = service.confirmImport(dto);

        assertEquals(3, result.importedCopies());
    }
}
