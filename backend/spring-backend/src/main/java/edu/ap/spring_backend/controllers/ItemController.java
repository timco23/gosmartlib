package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.service.BarcodeService;
import edu.ap.spring_backend.service.BookCsvImportService;
import edu.ap.spring_backend.service.ItemService;
import jakarta.validation.constraints.*;
import edu.ap.spring_backend.service.SchoolItemCopyService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.service.BookCoverCacheService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import java.time.Duration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.io.InputStream;

import java.util.List;

@Validated
@RestController
public class ItemController {
    private final ItemService itemService;
    private final SchoolItemCopyService schoolItemCopyService;
    private final BookCsvImportService bookCsvImportService;
    private final BarcodeService barcodeService;
    private final BookCoverCacheService bookCoverCacheService;

    /**
     * Default fallback book cover used when no cover image is available
     * or an error occurs while fetching the real cover.
     */
    private static final byte[] FALLBACK_COVER;

    /**
     * ETag for the fallback cover image.
     * This is generated using an MD5 hash of the cover content and is used
     * for HTTP caching (If-None-Match / 304 Not Modified).
     */
    private static final String FALLBACK_COVER_ETAG;

    //Fetch placeholder cover when this class gets loaded
    static {
        try (InputStream is = new ClassPathResource("placeholderCover/cover.jpg").getInputStream()) {
            FALLBACK_COVER = StreamUtils.copyToByteArray(is);
            FALLBACK_COVER_ETAG = strongEtag(FALLBACK_COVER);
        } catch (IOException e) {
            throw new RuntimeException("Could not load fallback book cover", e);
        }
    }

    private static String strongEtag(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return "\"" + HexFormat.of().formatHex(digest.digest(bytes)) + "\"";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public ItemController(ItemService itemService, SchoolItemCopyService schoolItemCopyService,
            BookCsvImportService bookCsvImportService, BarcodeService barcodeService,
            BookCoverCacheService bookCoverCacheService) {
        this.itemService = itemService;
        this.schoolItemCopyService = schoolItemCopyService;
        this.bookCsvImportService = bookCsvImportService;
        this.barcodeService = barcodeService;
        this.bookCoverCacheService = bookCoverCacheService;
    }

    @GetMapping("/books")
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    /**
     * Retrieves a paginated and filtered list of items based on various criteria.
     * Supports filtering by search term, languages, page count, score, tags,
     * genres, subgenres, didactic categories, and whether the item is didactic.
     * Also supports sorting by title, author, score, or pages in ascending or
     * descending order.
     * 
     * @param page
     * @param size
     * @param sortBy
     * @param sortDirection
     * @param schoolId
     * @param schoolSpecificBooks
     * @param search
     * @param languages
     * @param minPages
     * @param maxPages
     * @param minScore
     * @param tagIds
     * @param genreIds
     * @param subgenreIds
     * @param didacticCategoryIds
     * @param didactic
     * @param authentication
     * @return
     */
    @GetMapping("/filteredBooks")
    public ResponseEntity<PageResponseDTO<ItemDTO>> getAllFilteredItems(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(15) int size,
            @RequestParam(defaultValue = "title") @Pattern(regexp = "title|author|score|pages") String sortBy,
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "ASC|DESC", flags = Pattern.Flag.CASE_INSENSITIVE) String sortDirection,
            @RequestParam Long schoolId,
            @RequestParam Boolean schoolSpecificBooks,
            @RequestParam(required = false) @Size(max = 100) String search,
            @RequestParam(required = false) @Size(max = 10) List<String> languages,
            @RequestParam(required = false) @Min(0) Integer minPages,
            @RequestParam(required = false) @Min(0) Integer maxPages,
            @RequestParam(required = false) @DecimalMin("0.0") @DecimalMax("5.0") Double minScore,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) List<Long> genreIds,
            @RequestParam(required = false) List<Long> subgenreIds,
            @RequestParam(required = false) List<Long> didacticCategoryIds,
            @RequestParam(required = false) Boolean didactic,
            @RequestParam(required = false) Long campusId,
            Authentication authentication) {

        boolean isStudent = authentication != null && authentication.getAuthorities().stream()
                .map(a -> a.getAuthority() != null && a.getAuthority().startsWith("ROLE_")
                        ? a.getAuthority().substring(5)
                        : a.getAuthority())
                .anyMatch(auth -> Role.LEERLING.name().equalsIgnoreCase(auth));

        if (isStudent) {
            didactic = false;
            didacticCategoryIds = null;
        }

        ItemRequestDTO request = new ItemRequestDTO(
                page, size, sortBy, sortDirection, search, languages, minPages, maxPages, minScore,
                tagIds, genreIds, subgenreIds, didacticCategoryIds, didactic, schoolId, schoolSpecificBooks, campusId);

        return ResponseEntity.ok(itemService.getFilteredItems(request));
    }

    @GetMapping("/books/featured/{schoolId}")
    public ResponseEntity<List<ItemDTO>> getFeaturedItems(@PathVariable Long schoolId) {
        return ResponseEntity.ok(itemService.getFeaturedItems(schoolId));
    }

    @GetMapping("/books/search")
    public ResponseEntity<ItemDTO> getItemByTitle(@RequestParam String title) {
        return ResponseEntity.ok(itemService.getItemByTitle(title));
    }

    @GetMapping("/books/latest/{schoolId}")
    public ResponseEntity<List<ItemDTO>> getLatestItems(@PathVariable Long schoolId) {
        return ResponseEntity.ok(itemService.get10LatestItems(schoolId));
    }

    /**
     * Retrieves the book cover image for a given book ID.
     * This endpoint acts as a proxy with server-side caching and supports HTTP
     * caching via ETag.
     * Behavior:
     * - Returns 200 OK with image bytes if the cover is new or updated.
     * - Returns 304 Not Modified if the client already has the latest version
     * (based on ETag).
     * - Falls back to a default JPEG cover if the real cover cannot be retrieved.
     * Caching:
     * - Uses Cache-Control headers (public, max-age = 6 hours, must-revalidate).
     * - Uses ETag to reduce unnecessary data transfer.
     * 
     * @param id          the ID of the book
     * @param ifNoneMatch optional ETag from the client (If-None-Match header)
     * @return ResponseEntity containing:
     *         - image bytes (PNG/JPEG/SVG) with 200 OK, or
     *         - no body with 304 Not Modified
     */
    @GetMapping("/books/{id}/cover")
    public ResponseEntity<byte[]> getBookCover(
            @PathVariable Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        CacheControl cacheControl = CacheControl.maxAge(Duration.ofHours(6)).cachePublic().mustRevalidate();

        try {
            BookCoverResponse cover = bookCoverCacheService.getByItemId(id, ifNoneMatch);

            if (cover.notModified()) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .cacheControl(cacheControl)
                        .eTag(cover.etag())
                        .build();
            }

            return ResponseEntity.ok()
                    .cacheControl(cacheControl)
                    .eTag(cover.etag())
                    .contentType(MediaType.parseMediaType(cover.contentType()))
                    .body(cover.bytes());

       } catch (Exception ex) {
            if (ifNoneMatch != null && ifNoneMatch.contains(FALLBACK_COVER_ETAG)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .cacheControl(cacheControl)
                        .eTag(FALLBACK_COVER_ETAG)
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(FALLBACK_COVER);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .cacheControl(cacheControl)
                    .eTag(FALLBACK_COVER_ETAG)
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(FALLBACK_COVER);
        }
    }

    /**
     * Fetches a list of book copies based on their unique copy IDs.
     * This endpoint is designed to retrieve specific copies, often after they have been created (e.g., via CSV import) and the client has their IDs.
     * @param copyIds
     * @return a list of SchoolItemCopyDTOs corresponding to the provided copy IDs
     */
    @GetMapping("/books/copies")
    public ResponseEntity<List<SchoolItemCopyDTO>> getCopiesByIds(
            @RequestParam @Size(min = 1, max = 1000) List<Integer> copyIds) {
        return ResponseEntity.ok(schoolItemCopyService.getCopiesByIds(copyIds));
    }

    /**
     * Fetches a single book copy based on its unique copy ID.
     * @param copyId
     * @return a SchoolItemCopyDTO corresponding to the specified copy ID
     */
    @GetMapping("/books/copies/id/{copyId}")
    public ResponseEntity<SchoolItemCopyDTO> getCopyById(@PathVariable int copyId) {
        return ResponseEntity.ok(schoolItemCopyService.getCopyById(copyId));
    }

    /**
     * Fetches all copies of a book (item) for a specific school.
     * @param itemId
     * @param schoolId
     * @return a list of SchoolItemCopyDTOs corresponding to the specified item ID and school ID
     */
    @GetMapping("/books/copies/{itemId}")
    public ResponseEntity<List<SchoolItemCopyDTO>> getCopiesByItemIdAndSchoolId(
            @PathVariable Long itemId, @RequestParam Long schoolId) {
        return ResponseEntity.ok(schoolItemCopyService.getCopiesByItemIdAndSchoolId(itemId, schoolId));
    }

    /**
     * Fetches a single book copy based on its unique barcode string.
     * @param barcode
     * @return a SchoolItemCopyDTO corresponding to the specified barcode
     */
    @GetMapping("/books/copies/barcode/{barcode}")
    public ResponseEntity<SchoolItemCopyDTO> getCopyByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(schoolItemCopyService.getCopyByBarcode(barcode));
    }

    /**
     * Fetches a single book copy based on the book's title and the school ID.
     * @param title
     * @param schoolId
     * @return a SchoolItemCopyDTO corresponding to the specified book title and school ID
     */
    @GetMapping("/books/copies/search")
    public ResponseEntity<SchoolItemCopyDTO> getCopyByItemTitle(
            @RequestParam String title, @RequestParam Long schoolId) {
        return ResponseEntity.ok(schoolItemCopyService.getByItemTitleAndSchoolId(title, schoolId));
    }
    
    /**
     * Returns the barcode image (in PNG format) for a specific book copy based on its unique copy ID.
     * @param copyId
     * @return a ResponseEntity containing the PNG image bytes of the barcode corresponding to the specified copy ID
     */
    @GetMapping(value = "/books/copies/{copyId}/barcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBarcodePng(@PathVariable int copyId) {
        SchoolItemCopyDTO copy = schoolItemCopyService.getCopyById(copyId);
        return ResponseEntity.ok(barcodeService.generateBarcodePng(copy.barcode()));
    }

    @PostMapping("/books")
    public ResponseEntity<ItemDTO> addItem(@RequestBody CreateItemDTO itemDTO) {
        return ResponseEntity.ok(itemService.addItem(itemDTO));
    }
    
    @PostMapping("/books/import/preview")
    public ResponseEntity<CsvPreviewDTO> previewImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long schoolId) {
        return ResponseEntity.ok(bookCsvImportService.previewImport(file, schoolId));
    }

    @PostMapping("/books/import/confirm")
    public ResponseEntity<CsvImportDTO> confirmImport(@RequestBody CsvConfirmDTO dto) {
        return ResponseEntity.ok(bookCsvImportService.confirmImport(dto));
    }

    @PostMapping("/books/copies")
    public ResponseEntity<SchoolItemCopyDTO> addCopy(@RequestBody CreateSchoolItemCopyDTO copyDTO) {
        return ResponseEntity.ok(schoolItemCopyService.addCopy(copyDTO));
    }

    @PostMapping("/books/copies/bulk")
    public ResponseEntity<List<SchoolItemCopyDTO>> addCopiesBulk(
            @RequestBody List<CreateSchoolItemCopyDTO> copyDTOs) {
        return ResponseEntity.ok(schoolItemCopyService.addCopies(copyDTOs));
    }

    @PutMapping("/books/copies/{id}")
    public ResponseEntity<SchoolItemCopyDTO> updateCopy(
            @RequestBody CreateSchoolItemCopyDTO copyDTO, @PathVariable int id) {
        return ResponseEntity.ok(schoolItemCopyService.updateCopy(id, copyDTO));
    }

    @PutMapping("/books/copies/{copyId}/status")
    public ResponseEntity<SchoolItemCopyDTO> updateCopyStatus(
            @PathVariable int copyId, @RequestParam String status) {
        return ResponseEntity.ok(schoolItemCopyService.updateCopyStatus(copyId, status));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<ItemDTO> updateItem(@RequestBody UpdateItemDTO itemDTO,
            @PathVariable Long id) {
        return ResponseEntity.ok(itemService.updateItem(id, itemDTO));
    }

    @PutMapping("/books/copies/{copyId}/condition")
    public ResponseEntity<SchoolItemCopyDTO> updateCopyCondition(@PathVariable int copyId,
            @RequestParam String condition) {
        return ResponseEntity.ok(schoolItemCopyService.updateCopyCondition(copyId, condition));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<ItemDTO> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/books/copies/{id}")
    public ResponseEntity<SchoolItemCopyDTO> deleteCopy(@PathVariable int id) {
        schoolItemCopyService.deleteCopy(id);
        return ResponseEntity.noContent().build();
    }
}
