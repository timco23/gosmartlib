package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.CreateSchoolItemDTO;
import edu.ap.spring_backend.dto.ItemDTO;
import edu.ap.spring_backend.dto.SchoolItemCopyDTO;
import edu.ap.spring_backend.dto.SchoolItemDTO;
import edu.ap.spring_backend.dto.UpdateSchoolItemDTO;
import edu.ap.spring_backend.jpa.SchoolItemCopy;
import edu.ap.spring_backend.service.ItemService;
import edu.ap.spring_backend.service.SchoolItemCopyService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ItemController {
    private final ItemService itemService;
    private final SchoolItemCopyService schoolItemCopyService;
    public ItemController(ItemService itemService, SchoolItemCopyService schoolItemCopyService) {
        this.itemService = itemService;
        this.schoolItemCopyService = schoolItemCopyService;
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

    @GetMapping("/books/copies/{schoolItemId}")
    public ResponseEntity<List<SchoolItemCopy>> getCopiesBySchoolItemId(@PathVariable int schoolItemId) {
        return ResponseEntity.ok(schoolItemCopyService.getCopiesBySchoolItemId(schoolItemId));
    }

    @PostMapping("/books/copies")
    public ResponseEntity<SchoolItemCopy> addCopy(@RequestBody SchoolItemCopyDTO copyDTO) {
        return ResponseEntity.ok(schoolItemCopyService.addCopy(copyDTO));
    }

    @DeleteMapping("/books/copies/{id}")
    public ResponseEntity<SchoolItemCopy> deleteCopy(@PathVariable int id) {
        return ResponseEntity.ok(schoolItemCopyService.deleteCopy(id));
    }

    @PutMapping("/books/copies/{id}")
    public ResponseEntity<SchoolItemCopy> updateCopy(@RequestBody SchoolItemCopyDTO copyDTO, @PathVariable int id) {
        return ResponseEntity.ok(schoolItemCopyService.updateCopy(id, copyDTO));
    }
}
