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

import java.util.List;

@Validated
@RestController
public class ItemController {
    private final ItemService itemService;
    private final SchoolItemCopyService schoolItemCopyService;
    private final BookCsvImportService bookCsvImportService;
    private final BarcodeService barcodeService;

    public ItemController(ItemService itemService, SchoolItemCopyService schoolItemCopyService,
            BookCsvImportService bookCsvImportService, BarcodeService barcodeService) {
        this.itemService = itemService;
        this.schoolItemCopyService = schoolItemCopyService;
        this.bookCsvImportService = bookCsvImportService;
        this.barcodeService = barcodeService;
    }

    @GetMapping("/books")
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping("/filteredBooks")
    public ResponseEntity<PageResponseDTO<ItemDTO>> getAllFilteredItems(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size,
            @RequestParam(defaultValue = "title") @Pattern(regexp = "title|author|score|pages") String sortBy,
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "ASC|DESC", flags = Pattern.Flag.CASE_INSENSITIVE) String sortDirection,
            @RequestParam Long schoolId,
            @RequestParam(required = false) @Size(max = 100) String search,
            @RequestParam(required = false) @Size(max = 10) List<String> languages,
            @RequestParam(required = false) @Min(0) Integer minPages,
            @RequestParam(required = false) @Min(0) Integer maxPages,
            @RequestParam(required = false) @DecimalMin("0.0") @DecimalMax("5.0") Double minScore,
            @RequestParam(required = false) List<Long> tagIds) {
        ItemRequestDTO request = new ItemRequestDTO(
                page, size, sortBy, sortDirection,
                search, languages, minPages, maxPages, minScore, tagIds, schoolId);
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

    @DeleteMapping("/books/{id}")
    public ResponseEntity<ItemDTO> deleteItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.deleteItem(id));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<ItemDTO> updateItem(@RequestBody UpdateItemDTO itemDTO,
            @PathVariable Long id) {
        return ResponseEntity.ok(itemService.updateItem(id, itemDTO));
    }

    @GetMapping("/books/copies/id/{copyId}")
    public ResponseEntity<SchoolItemCopyDTO> getCopyById(@PathVariable int copyId) {
        return ResponseEntity.ok(schoolItemCopyService.getCopyById(copyId));
    }

    @GetMapping("/books/copies/{itemId}")
    public ResponseEntity<List<SchoolItemCopyDTO>> getCopiesByItemIdAndSchoolId(@PathVariable Long itemId, @RequestParam Long schoolId) {
        return ResponseEntity.ok(schoolItemCopyService.getCopiesByItemIdAndSchoolId(itemId, schoolId));
    }

    @PostMapping("/books/copies")
    public ResponseEntity<SchoolItemCopyDTO> addCopy(@RequestBody CreateSchoolItemCopyDTO copyDTO) {
        return ResponseEntity.ok(schoolItemCopyService.addCopy(copyDTO));
    }

    @PostMapping("/books/copies/bulk")
    public ResponseEntity<List<SchoolItemCopyDTO>> addCopiesBulk(@RequestBody List<CreateSchoolItemCopyDTO> copyDTOs) {
        return ResponseEntity.ok(schoolItemCopyService.addCopies(copyDTOs));
    }

    @DeleteMapping("/books/copies/{id}")
    public ResponseEntity<SchoolItemCopyDTO> deleteCopy(@PathVariable int id) {
        return ResponseEntity.ok(schoolItemCopyService.deleteCopy(id));
    }

    @PutMapping("/books/copies/{id}")
    public ResponseEntity<SchoolItemCopyDTO> updateCopy(@RequestBody CreateSchoolItemCopyDTO copyDTO, @PathVariable int id) {
        return ResponseEntity.ok(schoolItemCopyService.updateCopy(id, copyDTO));
    }

    @GetMapping("/books/copies/search")
    public ResponseEntity<SchoolItemCopyDTO> getCopyByItemTitle(@RequestParam String title, @RequestParam Long schoolId) {
        return ResponseEntity.ok(schoolItemCopyService.getByItemTitleAndSchoolId(title, schoolId));
    }

    @GetMapping("/books/copies/barcode/{barcode}")
    public ResponseEntity<SchoolItemCopyDTO> getCopyByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(schoolItemCopyService.getCopyByBarcode(barcode));
    }

    @PutMapping("/books/copies/{copyId}/status")
    public ResponseEntity<SchoolItemCopyDTO> updateCopyStatus(@PathVariable int copyId, @RequestParam String status) {
        return ResponseEntity.ok(schoolItemCopyService.updateCopyStatus(copyId, status));
    }

    @GetMapping(value = "/books/copies/{copyId}/barcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBarcodePng(@PathVariable int copyId) {
        SchoolItemCopyDTO copy = schoolItemCopyService.getCopyById(copyId);
        return ResponseEntity.ok(barcodeService.generateBarcodePng(copy.barcode()));
    }
}
