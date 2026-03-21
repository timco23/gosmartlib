package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.service.ItemService;
import jakarta.validation.constraints.*;
import edu.ap.spring_backend.jpa.SchoolItemCopy;
import edu.ap.spring_backend.service.SchoolItemCopyService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
public class ItemController {
    private final ItemService itemService;
    private final SchoolItemCopyService schoolItemCopyService;

    public ItemController(ItemService itemService, SchoolItemCopyService schoolItemCopyService) {
        this.itemService = itemService;
        this.schoolItemCopyService = schoolItemCopyService;
    }

    @GetMapping("/books")
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable int id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping("/filteredBooks")
    public ResponseEntity<PageResponseDTO<ItemDTO>> getAllFilteredItems(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size,
            @RequestParam(defaultValue = "title") @Pattern(regexp = "title|author|year|score|pages") String sortBy,
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "ASC|DESC", flags = Pattern.Flag.CASE_INSENSITIVE) String sortDirection,
            @RequestParam(required = false) @Size(max = 100) String search,
            @RequestParam(required = false) @Size(max = 10) List<String> languages,
            @RequestParam(required = false) @Min(0) Integer minPages,
            @RequestParam(required = false) @Min(0) Integer maxPages,
            @RequestParam(required = false) @Min(1500) @Max(2050) Integer minYear,
            @RequestParam(required = false) @Min(1500) @Max(2050) Integer maxYear,
            @RequestParam(required = false) @DecimalMin("0.0") @DecimalMax("5.0") Double minScore,
            @RequestParam(required = false) List<Long> tagIds) {
        ItemRequestDTO request = new ItemRequestDTO(
                page, size, sortBy, sortDirection,
                search, languages, minPages, maxPages, minYear, maxYear, minScore, tagIds);
        return ResponseEntity.ok(itemService.getFilteredItems(request));
    }

    @GetMapping("/books/featured")
    public ResponseEntity<List<ItemDTO>> getFeaturedItems() {
        return ResponseEntity.ok(itemService.getFeaturedItems());
    }

    @GetMapping("/books/search")
    public ResponseEntity<ItemDTO> getItemByTitle(@RequestParam String title) {
        return ResponseEntity.ok(itemService.getItemByTitle(title));
    }

    @PostMapping("/books")
    public ResponseEntity<ItemDTO> addItem(@RequestBody CreateItemDTO itemDTO) {
        return ResponseEntity.ok(itemService.addItem(itemDTO));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<ItemDTO> deleteItem(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.deleteItem(id));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<ItemDTO> updateItem(@RequestBody UpdateItemDTO itemDTO,
            @PathVariable int id) {
        return ResponseEntity.ok(itemService.updateItem(id, itemDTO));
    }

    @GetMapping("/books/copies/{itemId}")
    public ResponseEntity<List<SchoolItemCopy>> getCopiesByItemId(@PathVariable int itemId) {
        return ResponseEntity.ok(schoolItemCopyService.getCopiesByItemId(itemId));
    }

    @PostMapping("/books/copies")
    public ResponseEntity<SchoolItemCopy> addCopy(@RequestBody CreateSchoolItemCopyDTO copyDTO) {
        return ResponseEntity.ok(schoolItemCopyService.addCopy(copyDTO));
    }

    @DeleteMapping("/books/copies/{id}")
    public ResponseEntity<SchoolItemCopy> deleteCopy(@PathVariable int id) {
        return ResponseEntity.ok(schoolItemCopyService.deleteCopy(id));
    }

    @PutMapping("/books/copies/{id}")
    public ResponseEntity<SchoolItemCopy> updateCopy(@RequestBody CreateSchoolItemCopyDTO copyDTO, @PathVariable int id) {
        return ResponseEntity.ok(schoolItemCopyService.updateCopy(id, copyDTO));
    }
}
