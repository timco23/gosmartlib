package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.CreateReadingListDTO;
import edu.ap.spring_backend.dto.ReadingListDTO;
import edu.ap.spring_backend.service.ReadingListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lists")
public class ReadingListController {
    private final ReadingListService readingListService;
    public ReadingListController(ReadingListService readingListService) {
        this.readingListService = readingListService;
    }

    @GetMapping
    public ResponseEntity<List<ReadingListDTO>> getAllReadingList() {
        return ResponseEntity.ok(readingListService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadingListDTO> getReadingListById(@PathVariable Long id) {
        return ResponseEntity.ok(readingListService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReadingListDTO>> getReadingListByUserId(@PathVariable int userId) {
        return ResponseEntity.ok(readingListService.findByUserId(userId));
    }

    @GetMapping("/exists/{itemId}")
    public ResponseEntity<Boolean> existsByItemId(@PathVariable Long itemId) {
        return ResponseEntity.ok(readingListService.isItemInAnyReadingList(itemId));
    }

    @PostMapping
    public ResponseEntity<ReadingListDTO> addReadingList(@RequestBody CreateReadingListDTO readingListDTO) {
        return ResponseEntity.ok(readingListService.addReadingList(readingListDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReadingListDTO> updateReadingList(@RequestBody CreateReadingListDTO readingListDTO, @PathVariable Long id) {
        return ResponseEntity.ok(readingListService.updateReadingList(readingListDTO, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReadingListDTO> deleteReadingList(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
