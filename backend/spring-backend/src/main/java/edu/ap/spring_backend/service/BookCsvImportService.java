package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.BookImportRow;
import edu.ap.spring_backend.dto.CsvConfirmDTO;
import edu.ap.spring_backend.dto.CsvImportDTO;
import edu.ap.spring_backend.dto.CsvPreviewDTO;
import edu.ap.spring_backend.dto.RowError;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import edu.ap.spring_backend.dto.ItemEnrichmentEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
 * Titel;Auteur;ISBN;Conditie_nieuw;Conditie_goed;Conditie_gebruikt;Conditie_beschadigd;Campus
 *
 * Import follows a two-phase approach:
 *   Preview phase (previewImport):
 *       Parses and validates all rows
 *       all errors are collected and divided into a 'error' or 'criticalError' list before returning
 *       Returns the parsed rows so the user may optionally enrich them
 *       If a row's ISBN conflicts with an existing Item's ISBN, the existingIsbn field is set
 *   Confirm phase (confirmImport):
 *       Re-validates all rows, then writes them to the db
 *       For each row the service reuses an existing Item or creates a new one
 *       New items are saved with default values and enriched asynchronously after the transaction commits
 *       If updateExistingIsbn is true on a row, the existing Item's ISBN is updated
 *       For each condition, exactly the requested number of SchoolItemCopy records is created
 */
@Service
public class BookCsvImportService {

    private static final int EXPECTED_COLUMN_COUNT = 8;
    private static final int MAX_ROW_COUNT = 1000;
    private static final long MAX_FILE_SIZE_BYTES = 1024L * 1024; // 1 MB

    private static final String DEFAULT_LANGUAGE = "NEDERLANDS";
    private static final String DEFAULT_PUBLISHER = "Onbekend";
    private static final String DEFAULT_DESCRIPTION = "Geen beschrijving beschikbaar.";

    private final ItemRepository itemRepository;
    private final SchoolItemCopyRepository schoolItemCopyRepository;
    private final SchoolRepository schoolRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BookCsvImportService(ItemRepository itemRepository,
            SchoolItemCopyRepository schoolItemCopyRepository,
            SchoolRepository schoolRepository,
            ApplicationEventPublisher eventPublisher) {
        this.itemRepository = itemRepository;
        this.schoolItemCopyRepository = schoolItemCopyRepository;
        this.schoolRepository = schoolRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Validates the CSV file without writing anything to the db.
     * Rows are always returned so the user can enrich them; only critical errors result in an empty rows list.
     * On success, both error lists are empty and rows contains one entry per CSV row.
     *
     * @param file     the uploaded semicolon-delimited CSV file
     * @param schoolId the ID of the school whose catalogue the rows will be imported into
     * @return a CsvPreviewDTO containing either validation errors or the parsed rows
     */
    public CsvPreviewDTO previewImport(MultipartFile file, Long schoolId) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return new CsvPreviewDTO(List.of("Bestand is te groot. Maximum bestandsgrootte is 1 MB."), List.of(), List.of());
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            return new CsvPreviewDTO(List.of("Ongeldig bestandstype. Alleen .csv bestanden zijn toegestaan."), List.of(), List.of());
        }

        School school = schoolRepository.findById(schoolId).orElseThrow(EntityNotFoundException::new);

        List<String> criticalErrors = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();
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
            for (CSVRecord csvRecord : parser) {
                rowNum++;

                if (rowNum > MAX_ROW_COUNT + 1) {
                    criticalErrors.add("Bestand bevat meer dan " + MAX_ROW_COUNT + " rijen. Maximum is " + MAX_ROW_COUNT + " rijen.");
                    break;
                }

                if (csvRecord.size() != EXPECTED_COLUMN_COUNT) {
                    criticalErrors.add("Rij " + rowNum + ": incorrecte hoeveelheid kolommen (" + csvRecord.size()
                            + " gevonden, " + EXPECTED_COLUMN_COUNT + " verwacht).");
                    continue;
                }

                String title = getColumnValue(csvRecord, "Titel");
                String author = getColumnValue(csvRecord, "Auteur");
                String isbn = getColumnValue(csvRecord, "ISBN");
                String campus = getColumnValue(csvRecord, "Campus");

                int condNieuw    = parseCount(getColumnValue(csvRecord, "Conditie_nieuw"),      "Conditie_nieuw",      rowNum, errors);
                int condGoed     = parseCount(getColumnValue(csvRecord, "Conditie_goed"),       "Conditie_goed",       rowNum, errors);
                int condGebruikt = parseCount(getColumnValue(csvRecord, "Conditie_gebruikt"),   "Conditie_gebruikt",   rowNum, errors);
                int condBeschadigd = parseCount(getColumnValue(csvRecord, "Conditie_beschadigd"), "Conditie_beschadigd", rowNum, errors);

                String existingIsbn = detectIsbnConflict(title, isbn);

                BookImportRow row = new BookImportRow(rowNum, title, author, isbn,
                        condNieuw, condGoed, condGebruikt, condBeschadigd, campus, existingIsbn, false);
                validateRow(row, school, seenKeys, errors);
                rows.add(row);
            }

        } catch (IOException e) {
            return new CsvPreviewDTO(List.of("Fout bij verwerken van CSV-bestand."), List.of(), List.of());
        }

        if (!criticalErrors.isEmpty()) {
            return new CsvPreviewDTO(criticalErrors, List.of(), List.of());
        }

        return new CsvPreviewDTO(criticalErrors, errors, rows);
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
            return new CsvImportDTO(List.of(new RowError(0, "Te veel rijen. Maximum is " + MAX_ROW_COUNT + " rijen.")), 0, 0, List.of());
        }

        School school = schoolRepository.findById(dto.schoolId()).orElseThrow(EntityNotFoundException::new);

        List<RowError> errors = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        for (BookImportRow row : dto.rows()) {
            validateRow(row, school, seenKeys, errors);
        }
        if (!errors.isEmpty()) {
            return new CsvImportDTO(errors, 0, 0, List.of());
        }

        List<Integer> allCopyIds = new ArrayList<>();
        List<ItemEnrichmentEvent.TargetItem> toEnrich = new ArrayList<>();
        int totalCopies = 0;
        for (BookImportRow row : dto.rows()) {
            Item item = findOrCreateItem(row, toEnrich);
            if (row.updateExistingIsbn() && !row.isbn().isEmpty()) {
                item.setIsbn(row.isbn());
                itemRepository.save(item);
            }
            List<SchoolItemCopy> created = createCopies(item, school, row);
            created.forEach(c -> allCopyIds.add(c.getId()));
            totalCopies += row.conditionNieuw() + row.conditionGoed() + row.conditionGebruikt() + row.conditionBeschadigd();
        }

        if (!toEnrich.isEmpty()) {
            eventPublisher.publishEvent(new ItemEnrichmentEvent(toEnrich));
        }

        return new CsvImportDTO(List.of(), dto.rows().size(), totalCopies, allCopyIds);
    }

    /**
     * Returns an existing Item matched by title, or creates and saves a new one with default values.
     * New items with an ISBN are queued for asynchronous metadata enrichment via ItemEnrichmentEvent.
     * @param row the BookImportRow containing the title, author, ISBN and other data for the item to find or create
     * @param toEnrich a shared list of items to enrich after the transaction commits; if a new item is created with a non-empty ISBN, it is added to this list for later enrichment
     * @return an existing or new Item entity matching the title in the row; new items are saved with default values and queued for enrichment if they have an ISBN
     */
    private Item findOrCreateItem(BookImportRow row, List<ItemEnrichmentEvent.TargetItem> toEnrich) {
        Optional<Item> existing = itemRepository.findByTitle(row.title());
        if (existing.isPresent()) return existing.get();

        Item newItem = new Item(DEFAULT_DESCRIPTION, 0, 0.0, "", 0,
                DEFAULT_LANGUAGE, 0, row.isbn(), row.author(), DEFAULT_PUBLISHER, row.title(),
                new HashSet<>());
        itemRepository.save(newItem);

        if (!row.isbn().isEmpty()) {
            toEnrich.add(new ItemEnrichmentEvent.TargetItem(newItem.getId(), row.isbn()));
        }

        return newItem;
    }

    /**
     * Creates all SchoolItemCopy instances for the given row.
     * For each condition, exactly the requested number of copies is created.
     * @param item the Item entity that the copies will be associated with
     * @param school the School entity that the copies will belong to
     * @param row the BookImportRow containing the campus and condition counts for the copies to create
     * @return a list of all created SchoolItemCopy entities
     */
    private List<SchoolItemCopy> createCopies(Item item, School school, BookImportRow row) {
        Campus campus = school.getCampuses().stream()
                .filter(c -> normalize(c.getName()).equals(normalize(row.campus())))
                .findFirst()
                .orElse(null);

        List<SchoolItemCopy> created = new ArrayList<>();
        created.addAll(saveNCopies(item, school, campus, row.conditionNieuw(),      ItemCondition.NIEUW));
        created.addAll(saveNCopies(item, school, campus, row.conditionGoed(),        ItemCondition.GOED));
        created.addAll(saveNCopies(item, school, campus, row.conditionGebruikt(),    ItemCondition.GEBRUIKT));
        created.addAll(saveNCopies(item, school, campus, row.conditionBeschadigd(),  ItemCondition.BESCHADIGD));
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
     * @param row the BookImportRow to validate
     * @param school the School entity to validate against (e.g. for campus names)
     * @param seenKeys a shared set of normalized title+campus keys to detect duplicates within the file; the key for this row will be added to this set if it is valid
     * @param errors a shared list of RowError objects to which any validation errors will be appended; the row number is included in each error for later reporting to the user
     * @return void; errors are collected in the provided list, and the row's key is added to the seenKeys set if valid for duplicate detection
     */
    private void validateRow(BookImportRow row, School school, Set<String> seenKeys, List<RowError> errors) {
        int rowNum = row.rowNumber();

        if (row.title().isEmpty())
            errors.add(new RowError(rowNum, "'Titel' is verplicht."));
        if (row.author().isEmpty())
            errors.add(new RowError(rowNum, "'Auteur' is verplicht."));

        if (!row.isbn().isEmpty() && !row.isbn().matches("\\d{13}"))
            errors.add(new RowError(rowNum, "'ISBN' moet exact 13 cijfers bevatten."));

        if (row.conditionNieuw() < 0)
            errors.add(new RowError(rowNum, "'Conditie_nieuw' mag niet negatief zijn."));
        if (row.conditionGoed() < 0)
            errors.add(new RowError(rowNum, "'Conditie_goed' mag niet negatief zijn."));
        if (row.conditionGebruikt() < 0)
            errors.add(new RowError(rowNum, "'Conditie_gebruikt' mag niet negatief zijn."));
        if (row.conditionBeschadigd() < 0)
            errors.add(new RowError(rowNum, "'Conditie_beschadigd' mag niet negatief zijn."));

        int conditionSum = row.conditionNieuw() + row.conditionGoed() + row.conditionGebruikt() + row.conditionBeschadigd();
        if (conditionSum < 1)
            errors.add(new RowError(rowNum, "totaal aantal exemplaren moet minimaal 1 zijn."));

        if (!row.campus().isEmpty() && school.getCampuses().stream()
                .noneMatch(c -> normalize(c.getName()).equals(normalize(row.campus()))))
            errors.add(new RowError(rowNum, "campus '" + row.campus() + "' niet gevonden voor deze school."));

        String duplicateKey = row.title().toLowerCase() + "|" + normalize(row.campus());
        if (!row.title().isEmpty() && !seenKeys.add(duplicateKey))
            errors.add(new RowError(rowNum, "titel + campus komt meerdere keren voor in het bestand."));
    }

    /**
     * Returns the ISBN already stored in the db for the given title if it differs from the CSV ISBN,
     * or an empty string if there is no conflict
     * @param title the title to check for an existing Item with a different ISBN
     * @param isbn the ISBN from the CSV row to compare against the existing Item's ISBN
     * @return the existing ISBN if there is a conflict, or an empty string if there is no conflict or if the title or ISBN is empty
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

    private int parseCount(String value, String fieldName, int rowNum, List<RowError> errors) {
        if (value.isEmpty()) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            errors.add(new RowError(rowNum, "'" + fieldName + "' moet een getal zijn."));
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
