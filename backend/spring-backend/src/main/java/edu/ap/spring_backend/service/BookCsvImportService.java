package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.BookImportRow;
import edu.ap.spring_backend.dto.CsvConfirmDTO;
import edu.ap.spring_backend.dto.CsvImportDTO;
import edu.ap.spring_backend.dto.CsvPreviewDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import edu.ap.spring_backend.service.bookmetadata.BookMetadataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service responsible for bulk-importing books from a semicolon-delimited CSV file.
 * The expected CSV columns are:
 * Titel;Auteur;ISBN;Exemplaar_aantal;Conditie_nieuw;Conditie_goed;Conditie_gebruikt;Conditie_beschadigd;Campus
 *
 * Import follows a two-phase approach:
 *   Preview phase (previewImport):
 *       Parses and validates all rows; all errors are collected before returning
 *       Returns the parsed rows so the user may optionally enrich them
 *       If a row's ISBN conflicts with an existing Item's ISBN, the existingIsbn field is set
 *   Confirm phase (confirmImport):
 *       Re-validates all rows, then writes them to the db
 *       For each row the service reuses an existing Item or creates a new one
 *       New items are enriched with metadata from the Google Books API if an ISBN is present
 *       If updateExistingIsbn is true on a row, the existing Item's ISBN is updated
 *       One SchoolItemCopy is created per requested copy, distributed across condition options, remaining copies receive 'ONGEDEFINIEERD'
 */
@Service
public class BookCsvImportService {

    private static final int EXPECTED_COLUMN_COUNT = 9;
    private static final int MAX_ROW_COUNT = 1000;
    private static final long MAX_FILE_SIZE_BYTES = 1024 * 1024; // 1 MB

    private static final String DEFAULT_LANGUAGE = "NEDERLANDS";
    private static final String DEFAULT_PUBLISHER = "Onbekend";
    private static final String DEFAULT_DESCRIPTION = "Geen beschrijving beschikbaar.";

    private final ItemRepository itemRepository;
    private final SchoolItemCopyRepository schoolItemCopyRepository;
    private final SchoolRepository schoolRepository;
    private final BookMetadataService bookMetadataService;

    public BookCsvImportService(ItemRepository itemRepository,
            SchoolItemCopyRepository schoolItemCopyRepository,
            SchoolRepository schoolRepository,
            BookMetadataService bookMetadataService) {
        this.itemRepository = itemRepository;
        this.schoolItemCopyRepository = schoolItemCopyRepository;
        this.schoolRepository = schoolRepository;
        this.bookMetadataService = bookMetadataService;
    }

    /**
     * Validates the CSV file without writing anything to the db.
     * If any validation error is found the returned DTO contains the error list and an empty rows list.
     * On success, error list is empty and rows contains one entry per CSV row.
     *
     * @param file     the uploaded semicolon-delimited CSV file
     * @param schoolId the ID of the school whose catalogue the rows will be imported into
     * @return a CsvPreviewDTO containing either validation errors or the parsed rows
     */
    public CsvPreviewDTO previewImport(MultipartFile file, Long schoolId) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return new CsvPreviewDTO(List.of("Bestand is te groot. Maximum bestandsgrootte is 1 MB."), List.of());
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            return new CsvPreviewDTO(List.of("Ongeldig bestandstype. Alleen .csv bestanden zijn toegestaan."), List.of());
        }

        School school = schoolRepository.findById(schoolId).orElseThrow(EntityNotFoundException::new);

        List<String> errors = new ArrayList<>();
        List<BookImportRow> rows = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setDelimiter(";")
                        .setTrim(true)
                        .setIgnoreEmptyLines(true)
                        .build()
                        .parse(reader)) {

            int rowNum = 1;
            for (CSVRecord record : parser) {
                rowNum++;

                if (rowNum > MAX_ROW_COUNT + 1) {
                    errors.add("Bestand bevat meer dan " + MAX_ROW_COUNT + " rijen. Maximum is " + MAX_ROW_COUNT + " rijen.");
                    break;
                }

                if (record.size() != EXPECTED_COLUMN_COUNT) {
                    errors.add("Rij " + rowNum + ": incorrecte hoeveelheid kolommen (" + record.size()
                            + " gevonden, " + EXPECTED_COLUMN_COUNT + " verwacht).");
                    continue;
                }

                String title = getColumnValue(record, "Titel");
                String author = getColumnValue(record, "Auteur");
                String isbn = getColumnValue(record, "ISBN");
                String copiesStr = getColumnValue(record, "Exemplaar_aantal");
                String campus = getColumnValue(record, "Campus");

                int copies = 1;
                if (!copiesStr.isEmpty()) {
                    try {
                        copies = Integer.parseInt(copiesStr);
                    } catch (NumberFormatException e) {
                        errors.add("Rij " + rowNum + ": 'Exemplaar_aantal' moet een getal zijn.");
                    }
                }

                int condNieuw    = parseCount(getColumnValue(record, "Conditie_nieuw"),      "Conditie_nieuw",      rowNum, errors);
                int condGoed     = parseCount(getColumnValue(record, "Conditie_goed"),       "Conditie_goed",       rowNum, errors);
                int condGebruikt = parseCount(getColumnValue(record, "Conditie_gebruikt"),   "Conditie_gebruikt",   rowNum, errors);
                int condBeschadigd = parseCount(getColumnValue(record, "Conditie_beschadigd"), "Conditie_beschadigd", rowNum, errors);

                String existingIsbn = detectIsbnConflict(title, isbn);

                BookImportRow row = new BookImportRow(rowNum, title, author, isbn,
                        copies, condNieuw, condGoed, condGebruikt, condBeschadigd, campus, existingIsbn, false);
                validateRow(row, school, seenKeys, errors);
                rows.add(row);
            }

        } catch (Exception e) {
            return new CsvPreviewDTO(List.of("Fout bij verwerken van CSV-bestand."), List.of());
        }

        if (!errors.isEmpty()) {
            return new CsvPreviewDTO(errors, List.of());
        }

        return new CsvPreviewDTO(List.of(), rows);
    }

    /**
     * Re-validates and then writes a list of book import rows to the db
     * For each row the service reuses an existing Item or creates a new one,
     * then creates the requested number of SchoolItemCopy records
     *
     * @param dto contains the schoolId and the list of rows to import
     * @return a CsvImportDTO with the count of imported books and copies, or a non-empty errors list
     */
    @Transactional
    public CsvImportDTO confirmImport(CsvConfirmDTO dto) {
        if (dto.rows() == null || dto.rows().size() > MAX_ROW_COUNT) {
            return new CsvImportDTO(List.of("Te veel rijen. Maximum is " + MAX_ROW_COUNT + " rijen."), 0, 0, List.of());
        }

        School school = schoolRepository.findById(dto.schoolId()).orElseThrow(EntityNotFoundException::new);

        List<String> errors = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        for (BookImportRow row : dto.rows()) {
            validateRow(row, school, seenKeys, errors);
        }
        if (!errors.isEmpty()) {
            return new CsvImportDTO(errors, 0, 0, List.of());
        }

        List<Integer> allCopyIds = new ArrayList<>();
        int totalCopies = 0;
        for (BookImportRow row : dto.rows()) {
            Item item = findOrCreateItem(row);
            if (row.updateExistingIsbn() && !row.isbn().isEmpty()) {
                item.setIsbn(row.isbn());
                itemRepository.save(item);
            }
            List<SchoolItemCopy> created = createCopies(item, school, row);
            created.forEach(c -> allCopyIds.add(c.getId()));
            totalCopies += row.copies();
        }

        return new CsvImportDTO(List.of(), dto.rows().size(), totalCopies, allCopyIds);
    }

    /**
     * Returns an existing Item matched by title, or creates and saves a new one.
     * New items are enriched with metadata from the Google Books API when an ISBN is present.
     * Fields not returned by the API keep their default values.
     */
    private Item findOrCreateItem(BookImportRow row) {
        Optional<Item> existing = itemRepository.findByTitle(row.title());
        if (existing.isPresent()) return existing.get();

        String description = DEFAULT_DESCRIPTION;
        String publisher = DEFAULT_PUBLISHER;
        String language = DEFAULT_LANGUAGE;
        String cover = "";
        int pageCount = 0;
        int releaseYear = 0;

        if (!row.isbn().isEmpty()) {
            Optional<BookMetadata> metadata = bookMetadataService.fetchByIsbn(row.isbn());
            if (metadata.isPresent()) {
                BookMetadata m = metadata.get();
                if (m.description() != null) description = m.description();
                if (m.publisher()   != null) publisher   = m.publisher();
                if (m.language()    != null) language    = m.language();
                if (m.pageCount()   != null) pageCount   = m.pageCount();
                if (m.releaseYear() != null) releaseYear = m.releaseYear();
                if (m.coverUrl()    != null) cover       = m.coverUrl();
            }
        }

        Item newItem = new Item(description, 0, 0.0, cover, pageCount,
                language, releaseYear, row.isbn(), row.author(), publisher, row.title(),
                new HashSet<>());
        itemRepository.save(newItem);
        return newItem;
    }

    /**
     * Creates all SchoolItemCopy instances for the given row
     * Copies are distributed across the requested condition options,
     * any remainder receives 'ONGEDEFINIEERD'
     */
    private List<SchoolItemCopy> createCopies(Item item, School school, BookImportRow row) {
        Campus campus = school.getCampuses().stream()
                .filter(c -> normalize(c.getName()).equals(normalize(row.campus())))
                .findFirst()
                .orElse(null);

        int remaining = row.copies()
                - (row.conditionNieuw() + row.conditionGoed()
                        + row.conditionGebruikt() + row.conditionBeschadigd());

        List<SchoolItemCopy> created = new ArrayList<>();
        created.addAll(saveNCopies(item, school, campus, row.conditionNieuw(),      ItemCondition.NIEUW));
        created.addAll(saveNCopies(item, school, campus, row.conditionGoed(),        ItemCondition.GOED));
        created.addAll(saveNCopies(item, school, campus, row.conditionGebruikt(),    ItemCondition.GEBRUIKT));
        created.addAll(saveNCopies(item, school, campus, row.conditionBeschadigd(),  ItemCondition.BESCHADIGD));
        created.addAll(saveNCopies(item, school, campus, remaining,                  ItemCondition.ONGEDEFINIEERD));
        return created;
    }

    private List<SchoolItemCopy> saveNCopies(Item item, School school, Campus campus, int count, ItemCondition condition) {
        List<SchoolItemCopy> saved = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            SchoolItemCopy copy = new SchoolItemCopy(null, false, item, school, campus,
                    CopyStatus.BESCHIKBAAR, condition);
            SchoolItemCopy savedCopy = schoolItemCopyRepository.save(copy);
            savedCopy.setBarcode(String.format("BC%06d", savedCopy.getId()));
            saved.add(schoolItemCopyRepository.save(savedCopy));
        }
        return saved;
    }

    /**
     * Validates a BookImportRow against the school's campuses and the already-seen title+campus keys
     * Appends any errors to the provided list
     */
    private void validateRow(BookImportRow row, School school, Set<String> seenKeys, List<String> errors) {
        int rowNum = row.rowNumber();

        if (row.title().isEmpty())
            errors.add("Rij " + rowNum + ": 'Titel' is verplicht.");
        if (row.author().isEmpty())
            errors.add("Rij " + rowNum + ": 'Auteur' is verplicht.");

        if (!row.isbn().isEmpty() && !row.isbn().matches("\\d{13}"))
            errors.add("Rij " + rowNum + ": 'ISBN' moet exact 13 cijfers bevatten.");

        if (row.copies() < 1)
            errors.add("Rij " + rowNum + ": 'Exemplaar_aantal' moet een positief getal zijn.");

        if (row.conditionNieuw() < 0)
            errors.add("Rij " + rowNum + ": 'Conditie_nieuw' mag niet negatief zijn.");
        if (row.conditionGoed() < 0)
            errors.add("Rij " + rowNum + ": 'Conditie_goed' mag niet negatief zijn.");
        if (row.conditionGebruikt() < 0)
            errors.add("Rij " + rowNum + ": 'Conditie_gebruikt' mag niet negatief zijn.");
        if (row.conditionBeschadigd() < 0)
            errors.add("Rij " + rowNum + ": 'Conditie_beschadigd' mag niet negatief zijn.");

        int condSum = row.conditionNieuw() + row.conditionGoed()
                + row.conditionGebruikt() + row.conditionBeschadigd();
        if (condSum > row.copies())
            errors.add("Rij " + rowNum + ": som van conditie-aantallen (" + condSum
                    + ") overschrijdt 'Exemplaar_aantal' (" + row.copies() + ").");

        if (!row.campus().isEmpty() && school.getCampuses().stream()
                .noneMatch(c -> normalize(c.getName()).equals(normalize(row.campus()))))
            errors.add("Rij " + rowNum + ": campus '" + row.campus() + "' niet gevonden voor deze school.");

        String duplicateKey = row.title().toLowerCase() + "|" + normalize(row.campus());
        if (!row.title().isEmpty() && !seenKeys.add(duplicateKey))
            errors.add("Rij " + rowNum + ": titel + campus komt meerdere keren voor in het bestand.");
    }

    /**
     * Returns the ISBN already stored in the db for the given title if it differs from the CSV ISBN,
     * or an empty string if there is no conflict
     */
    private String detectIsbnConflict(String title, String isbn) {
        if (title.isEmpty() || isbn.isEmpty()) return "";
        return itemRepository.findByTitle(title)
                .map(Item::getIsbn)
                .filter(existing -> existing != null && !existing.isEmpty() && !existing.equals(isbn))
                .orElse("");
    }

    private static String normalize(String s) {
        return s.toLowerCase().replace(" ", "");
    }

    private int parseCount(String value, String fieldName, int rowNum, List<String> errors) {
        if (value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            errors.add("Rij " + rowNum + ": '" + fieldName + "' moet een getal zijn.");
            return 0;
        }
    }

    private String getColumnValue(CSVRecord record, String column) {
        try {
            return record.get(column).trim();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
