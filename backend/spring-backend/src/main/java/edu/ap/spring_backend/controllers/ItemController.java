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
    private ItemService itemService;
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("redirect:/books");
    }

    @GetMapping("/globalBooks")
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/books")
    public ResponseEntity<List<SchoolItem>> getAllSchoolItems() {
        return ResponseEntity.ok(itemService.getAllSchoolItems());
    }

    @PostMapping("/books")
    public ResponseEntity<SchoolItem> addItem(ItemDTO itemDTO) {
        return ResponseEntity.ok(itemService.addItem(itemDTO));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<SchoolItem> deleteItem(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.deleteSchoolItem(id));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<SchoolItem> updateItem(ItemDTO itemDTO, @PathVariable Integer id) {
        return ResponseEntity.ok(itemService.updateSchoolItem(id, itemDTO));
    }


}
