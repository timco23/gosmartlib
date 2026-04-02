package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.*;
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
            "Titel;Auteur;ISBN;Exemplaar_aantal;Conditie_nieuw;Conditie_goed;Conditie_gebruikt;Conditie_beschadigd;Campus;Taal;Beschrijving";

    @Mock private ItemRepository itemRepository;
    @Mock private SchoolItemCopyRepository schoolItemCopyRepository;
    @Mock private SchoolRepository schoolRepository;

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

    // previewImport

    @Test
    void givenValidRow_whenPreviewImport_thenRowParsedCorrectly() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "De Kleine Prins;Saint-Exupéry;;1;0;0;0;0;;NEDERLANDS;Een klassieker"), 1L);

        assertTrue(result.errors().isEmpty());
        assertEquals(1, result.rows().size());
        BookImportRow row = result.rows().get(0);
        assertEquals("De Kleine Prins", row.title());
        assertEquals("Saint-Exupéry", row.author());
        assertEquals(1, row.copies());
        assertEquals("NEDERLANDS", row.language());
        assertEquals("Een klassieker", row.description());
    }

    @Test
    void givenEmptyLanguage_whenPreviewImport_thenDefaultsToNederlands() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;0;;;"), 1L);

        assertTrue(result.errors().isEmpty());
        assertEquals("NEDERLANDS", result.rows().get(0).language());
    }

    @Test
    void givenEmptyCopies_whenPreviewImport_thenDefaultsToOne() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;;0;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().isEmpty());
        assertEquals(1, result.rows().get(0).copies());
    }

    @Test
    void givenValidLanguage_whenPreviewImport_thenNoError() {
        mockSchool();
        String[] languages = {"NEDERLANDS", "ENGELS", "FRANS", "DUITS", "ANDERE"};

        for (String lang : languages) {
            CsvPreviewDTO result = service.previewImport(
                    csv(HEADER, "Boek;Auteur;;1;0;0;0;0;;" + lang + ";"), 1L);
            assertTrue(result.errors().isEmpty(), "Expected no error for language: " + lang);
        }
    }

    @Test
    void givenValidIsbn13_whenPreviewImport_thenNoIsbnError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;9780140328721;1;0;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().isEmpty());
        assertEquals("9780140328721", result.rows().get(0).isbn());
    }

    @Test
    void givenEmptyIsbn_whenPreviewImport_thenNoIsbnError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().noneMatch(e -> e.contains("ISBN")));
    }

    @Test
    void givenConditionSumEqualsCopies_whenPreviewImport_thenNoError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;4;1;1;1;1;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().isEmpty());
    }

    @Test
    void givenConditionColumns_whenPreviewImport_thenConditionsParsedCorrectly() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;5;1;2;1;1;;NEDERLANDS;"), 1L);

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

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "Boek Een;Auteur A;;2;1;1;0;0;;NEDERLANDS;",
                "Boek Twee;Auteur B;;3;0;0;0;0;;ENGELS;"),
                1L);

        assertTrue(result.errors().isEmpty());
        assertEquals(2, result.rows().size());
        assertEquals("Boek Een", result.rows().get(0).title());
        assertEquals("Boek Twee", result.rows().get(1).title());
    }

    @Test
    void givenMultipleRows_whenPreviewImport_thenRowNumbersStartAtTwo() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "Boek A;Auteur;;1;0;0;0;0;;NEDERLANDS;",
                "Boek B;Auteur;;1;0;0;0;0;;ENGELS;"),
                1L);

        assertEquals(2, result.rows().get(0).rowNumber());
        assertEquals(3, result.rows().get(1).rowNumber());
    }

    @Test
    void givenKnownCampus_whenPreviewImport_thenNoError() {
        mockSchool();
        school.getCampuses().add(campus);

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;0;Ellermanstraat;NEDERLANDS;"), 1L);

        assertTrue(result.errors().isEmpty());
    }

    @Test
    void givenCampusWithDifferentCaseAndSpaces_whenPreviewImport_thenNormalizedAndMatched() {
        mockSchool();
        Campus spaced = new Campus();
        spaced.setName("Ellerman Straat");
        school.getCampuses().add(spaced);

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;0;Ellerman Straat;NEDERLANDS;"), 1L);

        assertTrue(result.errors().isEmpty());
    }

    @Test
    void givenSameTitleOnDifferentCampuses_whenPreviewImport_thenBothAllowed() {
        mockSchool();
        Campus campusA = new Campus(); campusA.setName("Campus A");
        Campus campusB = new Campus(); campusB.setName("Campus B");
        school.getCampuses().add(campusA);
        school.getCampuses().add(campusB);

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "Boek;Auteur;;1;0;0;0;0;Campus A;NEDERLANDS;",
                "Boek;Auteur;;1;0;0;0;0;Campus B;NEDERLANDS;"),
                1L);

        assertTrue(result.errors().isEmpty());
        assertEquals(2, result.rows().size());
    }

    // previewImport

    @Test
    void givenMissingTitle_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, ";Auteur;;1;0;0;0;0;;NEDERLANDS;"), 1L);

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'Titel' is verplicht")));
    }

    @Test
    void givenMissingAuthor_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;;;1;0;0;0;0;;NEDERLANDS;"), 1L);

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'Auteur' is verplicht")));
    }

    @Test
    void givenMissingTitleAndAuthor_whenPreviewImport_thenReturnsBothErrors() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, ";;;1;0;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'Titel' is verplicht")));
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'Auteur' is verplicht")));
    }

    @Test
    void givenInvalidLanguage_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;0;;KLINGON;"), 1L);

        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("ongeldige taal 'KLINGON'")));
    }

    @Test
    void givenIsbnTooShort_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;123;1;0;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'ISBN' moet exact 13 cijfers bevatten")));
    }

    @Test
    void givenIsbnWithLetters_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;978ABC3456789;1;0;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'ISBN' moet exact 13 cijfers bevatten")));
    }

    @Test
    void givenCopiesZero_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;0;0;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'Exemplaar_aantal' moet een positief getal zijn")));
    }

    @Test
    void givenCopiesNegative_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;-3;0;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'Exemplaar_aantal' moet een positief getal zijn")));
    }

    @Test
    void givenCopiesNotANumber_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;abc;0;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'Exemplaar_aantal' moet een getal zijn")));
    }

    @Test
    void givenConditionSumExceedsCopies_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;2;2;1;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("som van conditie-aantallen")));
    }

    @Test
    void givenNegativeCondition_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;3;-1;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("mag niet negatief zijn")));
    }

    @Test
    void givenConditionNotANumber_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;3;abc;0;0;0;;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'Conditie_nieuw' moet een getal zijn")));
    }

    @Test
    void givenAllConditionColumnsInvalid_whenPreviewImport_thenAllFourErrorsReturned() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;10;x;y;z;w;;NEDERLANDS;"), 1L);

        assertEquals(4, result.errors().stream()
                .filter(e -> e.contains("moet een getal zijn")).count());
    }

    @Test
    void givenUnknownCampus_whenPreviewImport_thenReturnsError() {
        mockSchool();
        school.getCampuses().add(campus);

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;;1;0;0;0;0;onbekendcampus;NEDERLANDS;"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("niet gevonden voor deze school")));
    }

    @Test
    void givenDuplicateTitleSameCampus_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "Boek;Auteur;;1;0;0;0;0;;NEDERLANDS;",
                "Boek;Auteur;;1;0;0;0;0;;NEDERLANDS;"),
                1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("titel + campus komt meerdere keren voor")));
    }

    @Test
    void givenDuplicateTitleDifferentCase_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "boek;Auteur;;1;0;0;0;0;;NEDERLANDS;",
                "BOEK;Auteur;;1;0;0;0;0;;NEDERLANDS;"),
                1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("titel + campus komt meerdere keren voor")));
    }

    @Test
    void givenWrongColumnCount_whenPreviewImport_thenReturnsError() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, "Boek;Auteur;te;veel;kolom;men;hier;echt;te;veel;zeker;extra"), 1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("incorrecte hoeveelheid kolommen")));
    }

    @Test
    void givenRowWithWrongColumnCount_whenPreviewImport_thenRowSkippedAndOtherRowsValidated() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(csv(
                HEADER,
                "Boek;Auteur;te;veel;kolommen;hier;echt;te;veel;zeker;extra;twaalf",
                ";Auteur;;1;0;0;0;0;;NEDERLANDS;"),
                1L);

        assertTrue(result.errors().stream().anyMatch(e -> e.contains("incorrecte hoeveelheid kolommen")));
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("'Titel' is verplicht")));
    }

    @Test
    void givenMultipleErrorsOnSameRow_whenPreviewImport_thenAllErrorsCollected() {
        mockSchool();

        // Missing title, missing author, invalid language, copies=0, negative condition
        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, ";;;0;-1;0;0;0;;KLINGON;"), 1L);

        assertTrue(result.errors().size() >= 4);
    }

    @Test
    void givenUnknownSchoolId_whenPreviewImport_thenThrowsEntityNotFoundException() {
        when(schoolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.previewImport(csv(HEADER, "Boek;Auteur;;1;0;0;0;0;;NEDERLANDS;"), 99L));
    }

    @Test
    void givenValidationErrors_whenPreviewImport_thenRowsListIsEmpty() {
        mockSchool();

        CsvPreviewDTO result = service.previewImport(
                csv(HEADER, ";Auteur;;1;0;0;0;0;;NEDERLANDS;"), 1L);

        assertFalse(result.errors().isEmpty());
        assertTrue(result.rows().isEmpty());
    }

    // ── confirmImport ─────────────────────────────────────────────────────────

    @Test
    void givenNewItem_whenConfirmImport_thenItemSavedAndCopyCreated() {
        mockSchool();
        when(itemRepository.findByTitle("Nieuw Boek")).thenReturn(Optional.empty());
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Nieuw Boek", "Auteur", "", 1, 1, 0, 0, 0, "", "NEDERLANDS", "desc")));

        CsvImportDTO result = service.confirmImport(dto);

        assertTrue(result.errors().isEmpty());
        assertEquals(1, result.importedBooks());
        assertEquals(1, result.importedCopies());
        verify(itemRepository).save(any(Item.class));
        verify(schoolItemCopyRepository, times(1)).save(any(SchoolItemCopy.class));
    }

    @Test
    void givenExistingItem_whenConfirmImport_thenItemNotSavedAgain() {
        mockSchool();
        when(itemRepository.findByTitle("Bestaand Boek")).thenReturn(Optional.of(stubItem("Bestaand Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Bestaand Boek", "Auteur", "", 1, 1, 0, 0, 0, "", "NEDERLANDS", "")));

        service.confirmImport(dto);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void givenConditionDistribution_whenConfirmImport_thenCopiesCreatedWithCorrectConditions() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(stubItem("Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // 5 copies: 1 NIEUW, 1 GOED, 1 GEBRUIKT, 1 BESCHADIGD, 1 ONGEDEFINIEERD remainder
        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 5, 1, 1, 1, 1, "", "NEDERLANDS", "")));

        service.confirmImport(dto);

        ArgumentCaptor<SchoolItemCopy> captor = ArgumentCaptor.forClass(SchoolItemCopy.class);
        verify(schoolItemCopyRepository, times(5)).save(captor.capture());

        List<ItemCondition> conditions = captor.getAllValues().stream()
                .map(SchoolItemCopy::getCondition).toList();
        assertTrue(conditions.contains(ItemCondition.NIEUW));
        assertTrue(conditions.contains(ItemCondition.GOED));
        assertTrue(conditions.contains(ItemCondition.GEBRUIKT));
        assertTrue(conditions.contains(ItemCondition.BESCHADIGD));
        assertTrue(conditions.contains(ItemCondition.ONGEDEFINIEERD));
    }

    @Test
    void givenNoConditionsSpecified_whenConfirmImport_thenAllCopiesOngedefinieerd() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(stubItem("Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 3, 0, 0, 0, 0, "", "NEDERLANDS", "")));

        service.confirmImport(dto);

        ArgumentCaptor<SchoolItemCopy> captor = ArgumentCaptor.forClass(SchoolItemCopy.class);
        verify(schoolItemCopyRepository, times(3)).save(captor.capture());
        assertTrue(captor.getAllValues().stream()
                .allMatch(c -> c.getCondition() == ItemCondition.ONGEDEFINIEERD));
    }

    @Test
    void givenKnownCampus_whenConfirmImport_thenCampusResolvedOnCopy() {
        mockSchool();
        school.getCampuses().add(campus);
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(stubItem("Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 1, 1, 0, 0, 0, "ellermanstraat", "NEDERLANDS", "")));

        service.confirmImport(dto);

        ArgumentCaptor<SchoolItemCopy> captor = ArgumentCaptor.forClass(SchoolItemCopy.class);
        verify(schoolItemCopyRepository).save(captor.capture());
        assertEquals(campus, captor.getValue().getCampus());
    }

    @Test
    void givenNoCampusMatch_whenConfirmImport_thenNullCampusOnCopy() {
        mockSchool();
        school.getCampuses().add(campus);
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(stubItem("Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 1, 0, 0, 0, 0, "", "NEDERLANDS", "")));

        service.confirmImport(dto);

        ArgumentCaptor<SchoolItemCopy> captor = ArgumentCaptor.forClass(SchoolItemCopy.class);
        verify(schoolItemCopyRepository).save(captor.capture());
        assertNull(captor.getValue().getCampus());
    }

    @Test
    void givenMultipleRows_whenConfirmImport_thenCountsSummedCorrectly() {
        mockSchool();
        when(itemRepository.findByTitle("Boek A")).thenReturn(Optional.of(stubItem("Boek A")));
        when(itemRepository.findByTitle("Boek B")).thenReturn(Optional.of(stubItem("Boek B")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek A", "A1", "", 3, 0, 0, 0, 0, "", "NEDERLANDS", ""),
                new BookImportRow(3, "Boek B", "A2", "", 2, 0, 0, 0, 0, "", "ENGELS", "")));

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
                new BookImportRow(2, "Boek", "Auteur", "", 2, 1, 1, 0, 0, "", "NEDERLANDS", "")));

        service.confirmImport(dto);

        ArgumentCaptor<SchoolItemCopy> captor = ArgumentCaptor.forClass(SchoolItemCopy.class);
        verify(schoolItemCopyRepository, times(2)).save(captor.capture());
        assertTrue(captor.getAllValues().stream()
                .allMatch(c -> c.getCopy_status() == CopyStatus.BESCHIKBAAR));
    }

    @Test
    void givenNewItem_whenConfirmImport_thenItemCreatedWithRowMetadata() {
        mockSchool();
        when(itemRepository.findByTitle("Mijn Boek")).thenReturn(Optional.empty());
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Mijn Boek", "Schrijver", "", 1, 0, 0, 0, 0, "", "ENGELS", "Mooie beschrijving")));

        service.confirmImport(dto);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        assertEquals("Mijn Boek", captor.getValue().getTitle());
        assertEquals("Schrijver", captor.getValue().getAuthor());
        assertEquals("ENGELS", captor.getValue().getBookLanguage());
        assertEquals("Mooie beschrijving", captor.getValue().getDescription());
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

    @Test
    void givenCopiesWithPartialConditions_whenConfirmImport_thenExactCopyCountSaved() {
        mockSchool();
        when(itemRepository.findByTitle("Boek")).thenReturn(Optional.of(stubItem("Boek")));
        when(schoolItemCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CsvConfirmDTO dto = new CsvConfirmDTO(1L, List.of(
                new BookImportRow(2, "Boek", "Auteur", "", 4, 2, 1, 0, 0, "", "NEDERLANDS", "")));

        service.confirmImport(dto);

        // 2 NIEUW + 1 GOED + 0 GEBRUIKT + 0 BESCHADIGD + 1 ONGEDEFINIEERD = 4
        verify(schoolItemCopyRepository, times(4)).save(any(SchoolItemCopy.class));
    }
}
