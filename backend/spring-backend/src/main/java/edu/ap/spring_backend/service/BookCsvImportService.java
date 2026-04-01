package edu.ap.spring_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ap.spring_backend.dto.GoogleBooksResponse;
import edu.ap.spring_backend.dto.GoogleBooksResponse.VolumeInfo;
import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.BookImportRow;
import edu.ap.spring_backend.dto.CsvImportDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service responsible for bulk-importing books from a semicolon-delimited CSV file.
 * The expected CSV columns are:
 * Titel;Auteur;ISBN;Exemplaar_aantal;Conditie_nieuw;Conditie_goed;Conditie_gebruikt;Conditie_beschadigd;Campus;Taal;Beschrijving
 * Import follows a two-phase approach:
 *   Validation phase
 *       All errors are collected and returned together
 *       a single error aborts the entire import
 *   Import phase
 *       For each row the service attempts to reuse an existing Item matched by title.
 *       If no match is found and an ISBN is present, it calls the Google Books API to autofill
 *       optional metadata (publisher, description, page count, release year, cover).
 *       Any fields still empty after both recovery steps receive a default value.
 *       One SchoolItemCopy is created per requested copy, distributed across the
 *       supplied condition counts, remaining copies receive condition ONGEDEFINIEERD
 */
@Service
public class BookCsvImportService {

    private static final Set<String> VALID_LANGUAGES = Set.of(
            "NEDERLANDS", "FRANS", "ENGELS", "DUITS", "ANDERE");
    private static final String DEFAULT_LANGUAGE = "NEDERLANDS";
    private static final int EXPECTED_COLUMN_COUNT = 11;
    private static final String GOOGLE_BOOKS_URL =
            "https://www.googleapis.com/books/v1/volumes?q=isbn:";

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Random RANDOM = new Random();

    private final ItemRepository itemRepository;
    private final SchoolItemCopyRepository schoolItemCopyRepository;
    private final SchoolRepository schoolRepository;

    public BookCsvImportService(ItemRepository itemRepository,
            SchoolItemCopyRepository schoolItemCopyRepository,
            SchoolRepository schoolRepository) {
        this.itemRepository = itemRepository;
        this.schoolItemCopyRepository = schoolItemCopyRepository;
        this.schoolRepository = schoolRepository;
    }

    /**
     * Parses, validates, and imports books from the given CSV file into the specified school
     * If any validation error is found the method returns immediately with an errors list and an empty importedTitles list
     * On success, errors is empty and importedTitles lists every imported title
     *
     * @param file     the uploaded semicolon-delimited CSV file
     * @param schoolId the ID of the school whose catalogue receives the imported copies
     * @return a CsvImportDTO containing either validation errors or the imported titles
     */
    @Transactional
    public CsvImportDTO importBooks(MultipartFile file, Long schoolId) {
        School school = schoolRepository.findById(schoolId).orElseThrow(EntityNotFoundException::new);

        List<String> errors = new ArrayList<>();
        List<BookImportRow> rows = new ArrayList<>();
        Set<String> seenTitles = new HashSet<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setDelimiter(';')
                        .setTrim(true)
                        .setIgnoreEmptyLines(true)
                        .build()
                        .parse(reader)) {

            int rowNum = 1;
            for (CSVRecord record : parser) {
                rowNum++;

                if (record.size() != EXPECTED_COLUMN_COUNT) {
                    errors.add("Rij " + rowNum + ": incorrecte hoeveelheid kolommen (" + record.size()
                            + " gevonden, " + EXPECTED_COLUMN_COUNT + " verwacht).");
                    continue;
                }

                String title = getColumnValue(record, "Titel");
                String author = getColumnValue(record, "Auteur");
                String isbn = getColumnValue(record, "ISBN");
                String copiesStr = getColumnValue(record, "Exemplaar_aantal");
                String condNieuwStr = getColumnValue(record, "Conditie_nieuw");
                String condGoedStr = getColumnValue(record, "Conditie_goed");
                String condGebruiktStr = getColumnValue(record, "Conditie_gebruikt");
                String condBeschadigdStr = getColumnValue(record, "Conditie_beschadigd");
                String campus = getColumnValue(record, "Campus").toLowerCase().replace(" ", "");
                String language = getColumnValue(record, "Taal").toUpperCase();
                String description = getColumnValue(record, "Beschrijving");

                // Required fields
                if (title.isEmpty())
                    errors.add("Rij " + rowNum + ": 'Titel' is verplicht.");
                if (author.isEmpty())
                    errors.add("Rij " + rowNum + ": 'Auteur' is verplicht.");

                // Language: optional, defaults to NEDERLANDS
                if (!language.isEmpty() && !VALID_LANGUAGES.contains(language)) {
                    errors.add("Rij " + rowNum + ": ongeldige taal '" + language
                            + "'. Geldige waarden: " + String.join(", ", VALID_LANGUAGES) + ".");
                }
                if (language.isEmpty()) {
                    language = DEFAULT_LANGUAGE;
                }

                // ISBN format
                if (!isbn.isEmpty() && !isbn.matches("\\d{13}")) {
                    errors.add("Rij " + rowNum + ": 'ISBN' moet exact 13 cijfers bevatten.");
                }

                // copy amount
                int copies = 1;
                if (!copiesStr.isEmpty()) {
                    try {
                        copies = Integer.parseInt(copiesStr);
                        if (copies < 1)
                            errors.add("Rij " + rowNum + ": 'Exemplaar_aantal' moet een positief getal zijn.");
                    } catch (NumberFormatException e) {
                        errors.add("Rij " + rowNum + ": 'Exemplaar_aantal' moet een getal zijn.");
                    }
                }

                // Condition columns
                int condNieuw = parseCount(condNieuwStr, "Conditie_nieuw", rowNum, errors);
                int condGoed = parseCount(condGoedStr, "Conditie_goed", rowNum, errors);
                int condGebruikt = parseCount(condGebruiktStr, "Conditie_gebruikt", rowNum, errors);
                int condBeschadigd = parseCount(condBeschadigdStr, "Conditie_beschadigd", rowNum, errors);

                int condSum = condNieuw + condGoed + condGebruikt + condBeschadigd;
                if (condSum > copies) {
                    errors.add("Rij " + rowNum + ": som van conditie-aantallen (" + condSum
                            + ") overschrijdt 'Exemplaar_aantal' (" + copies + ").");
                }

                // Campus must match a campus of the school when provided
                if (!campus.isEmpty() && school.getCampuses().stream()
                        .noneMatch(c -> c.getName().toLowerCase().replace(" ", "").equals(campus))) {
                    errors.add("Rij " + rowNum + ": campus '" + campus + "' niet gevonden voor deze school.");
                }

                // Duplicate within file
                if (!title.isEmpty() && !seenTitles.add(title.toLowerCase())) {
                    errors.add("Rij " + rowNum + ": '" + title + "' komt meerdere keren voor in het bestand.");
                }

                rows.add(new BookImportRow(rowNum, title, author, isbn,
                        copies, condNieuw, condGoed, condGebruikt, condBeschadigd, campus, language, description));
            }

        } catch (Exception e) {
            return new CsvImportDTO(
                    List.of("Fout bij verwerken van CSV-bestand: " + e.getMessage()), List.of());
        }

        if (!errors.isEmpty()) {
            return new CsvImportDTO(errors, List.of());
        }

        // All rows valid, start import
        List<String> importedTitles = new ArrayList<>();

        for (BookImportRow row : rows) {
            Item item = findOrCreateItem(row);
            createCopies(item, school, row);
            importedTitles.add(row.title());
        }

        return new CsvImportDTO(List.of(), importedTitles);
    }

    /**
     * Returns an existing Item matched by title, or creates a new one
     * When creating a new item, optional metadata is requested from the Google Books API
     * using the row's ISBN. Fields not returned by the API keep their default values.
     */
    private Item findOrCreateItem(BookImportRow row) {
        Optional<Item> existing = itemRepository.findByTitle(row.title());
        if (existing.isPresent()) {
            return existing.get();
        }

        //defaults
        String publisher = "Niet ingesteld";
        String description = row.description();
        int releaseYear = 0;
        int pageCount = 0;
        String cover = "";

        if (!row.isbn().isEmpty()) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GOOGLE_BOOKS_URL + row.isbn()))
                        .GET()
                        .build();
                HttpResponse<String> response =
                        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                GoogleBooksResponse googleResponse =
                        OBJECT_MAPPER.readValue(response.body(), GoogleBooksResponse.class);
                if (googleResponse.totalItems() > 0
                        && googleResponse.items() != null
                        && !googleResponse.items().isEmpty()) {
                    VolumeInfo info = googleResponse.items().get(0).volumeInfo();
                    if (info != null) {
                        if (description.isEmpty() && info.description() != null)
                            description = info.description();
                        if (info.publisher() != null)
                            publisher = info.publisher();
                        if (info.pageCount() > 0)
                            pageCount = info.pageCount();
                        if (info.publishedDate() != null && info.publishedDate().length() >= 4) {
                            try {
                                releaseYear = Integer.parseInt(info.publishedDate().substring(0, 4));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        if (info.imageLinks() != null && info.imageLinks().thumbnail() != null)
                            cover = info.imageLinks().thumbnail();
                    }
                }
            } catch (Exception ignored) {
                // API failure, proceed with defaults
            }
        }

        Item newItem = new Item(description, 0, 0.0, cover, pageCount,
                row.language(), releaseYear, row.isbn(), row.author(), publisher, row.title(),
                new HashSet<>());
        itemRepository.save(newItem);
        return newItem;
    }

    /**
     * Creates all SchoolItemCopy instances for the given row
     * Copies are distributed across the requested conditions;
     * any remainder receives ONGEDEFINIEERD
     */
    private void createCopies(Item item, School school, BookImportRow row) {
        Campus campus = school.getCampuses().stream()
                .filter(c -> c.getName().toLowerCase().replace(" ", "").equals(row.campus()))
                .findFirst()
                .orElse(null);

        int remaining = row.copies()
                - (row.conditionNieuw() + row.conditionGoed()
                        + row.conditionGebruikt() + row.conditionBeschadigd());

        saveNCopies(item, school, campus, row.conditionNieuw(), ItemCondition.NIEUW);
        saveNCopies(item, school, campus, row.conditionGoed(), ItemCondition.GOED);
        saveNCopies(item, school, campus, row.conditionGebruikt(), ItemCondition.GEBRUIKT);
        saveNCopies(item, school, campus, row.conditionBeschadigd(), ItemCondition.BESCHADIGD);
        saveNCopies(item, school, campus, remaining, ItemCondition.ONGEDEFINIEERD);
    }

    // REPLACE WITH GENERATED BARCODES
    // currently generates a random number
    private void saveNCopies(Item item, School school, Campus campus, int count, ItemCondition condition) {
        for (int i = 0; i < count; i++) {
            String barcode = String.valueOf(RANDOM.nextLong(1_000_000_000L, 10_000_000_000L));
            schoolItemCopyRepository.save(
                    new SchoolItemCopy(barcode, false, item, school, campus,
                            CopyStatus.BESCHIKBAAR, condition));
        }
    }

    private int parseCount(String value, String fieldName, int rowNum, List<String> errors) {
        if (value.isEmpty()) return 0;
        try {
            int count = Integer.parseInt(value);
            if (count < 0) {
                errors.add("Rij " + rowNum + ": '" + fieldName + "' mag niet negatief zijn.");
                return 0;
            }
            return count;
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
