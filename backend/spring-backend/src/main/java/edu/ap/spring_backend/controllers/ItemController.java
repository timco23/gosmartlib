package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.entity.Item;
import edu.ap.spring_backend.entity.SchoolItem;
import edu.ap.spring_backend.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
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
    public ResponseEntity<List<SchoolItemDTO>> getAllSchoolItems() {
        return ResponseEntity.ok(itemService.getAllSchoolItems());
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
