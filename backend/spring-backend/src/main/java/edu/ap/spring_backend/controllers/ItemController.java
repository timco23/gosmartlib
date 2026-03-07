package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int size
    ) {
        return ResponseEntity.ok(itemService.getAllSchoolItems(page, size));
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
