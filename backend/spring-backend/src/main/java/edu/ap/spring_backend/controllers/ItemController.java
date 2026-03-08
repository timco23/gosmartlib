package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.service.ItemService;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
public class ItemController {
    private final ItemService itemService;
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/globalBooks")
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/globalBooks/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable int id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping("/books")
    public ResponseEntity<PageResponseDTO<SchoolItemDTO>> getAllSchoolItems(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size,
            @RequestParam(defaultValue= "title") @Pattern(regexp = "title|author|year|score|pages") String sortBy,
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "ASC|DESC", flags = Pattern.Flag.CASE_INSENSITIVE) String sortDirection,
            @RequestParam(required = false) @Size(max = 100) String search,
            @RequestParam(required = false) @Size(max = 10) List<String> languages,
            @RequestParam(required = false) @Min(0) Integer minPages,
            @RequestParam(required = false) @Min(0) Integer maxPages,
            @RequestParam(required = false) @Min(1500) @Max(2050) Integer minYear,
            @RequestParam(required = false) @Min(1500) @Max(2050) Integer maxYear,
            @RequestParam(required = false) @DecimalMin("0.0") @DecimalMax("5.0") Double minScore
    ) {
        SchoolItemRequestDTO request = new SchoolItemRequestDTO(
                page, size, sortBy, sortDirection,
                search, languages, minPages, maxPages, minYear, maxYear, minScore
        );
        return ResponseEntity.ok(itemService.getSchoolItems(request));
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<SchoolItemDTO> getSchoolItemById(@PathVariable int id) {
        return ResponseEntity.ok(itemService.getSchoolItemById(id));
    }

    @GetMapping("/books/search")
    public ResponseEntity<SchoolItemDTO> getSchoolItemByTitle(@RequestParam String title) {
        return ResponseEntity.ok(itemService.getSchoolItemByTitle(title));
    }

    @PostMapping("/books")
    public ResponseEntity<SchoolItemDTO> addItem(@RequestBody CreateSchoolItemDTO schoolItemDTO) {
        return ResponseEntity.ok(itemService.addSchoolItem(schoolItemDTO));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<SchoolItemDTO> deleteItem(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.deleteSchoolItem(id));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<SchoolItemDTO> updateItem(@RequestBody UpdateSchoolItemDTO schoolItemDTO, @PathVariable int id) {
        return ResponseEntity.ok(itemService.updateSchoolItem(id, schoolItemDTO));
    }


}
