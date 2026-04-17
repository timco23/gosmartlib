package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.CollectionDTO;
import edu.ap.spring_backend.dto.CreateCollectionDTO;
import edu.ap.spring_backend.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class CollectionController {
    private final CollectionService collectionService;
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<List<CollectionDTO>> getAllCollectionsBySchoolId(@PathVariable Long schoolId) {
        return ResponseEntity.ok(collectionService.findBySchoolId(schoolId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionDTO> getCollectionById(@PathVariable Long id) {
        return ResponseEntity.ok(collectionService.findById(id));
    }

    @GetMapping("/school/{schoolId}/featured")
    public ResponseEntity<List<CollectionDTO>> getFeaturedCollectionsBySchoolId(@PathVariable Long schoolId) {
        return ResponseEntity.ok(collectionService.getFeaturedBySchoolId(schoolId));
    }

    @GetMapping("/global/featured")
    public ResponseEntity<List<CollectionDTO>> getGloballyFeaturedCollections() {
        return ResponseEntity.ok(collectionService.getByGloballyFeatured());
    }

    @GetMapping("/exists/{itemId}")
    public ResponseEntity<Boolean> existsByItemId(@PathVariable Long itemId) {
        return ResponseEntity.ok(collectionService.isItemInAnyCollection(itemId));
    }

    @GetMapping("/global/exists/{itemId}/")
    public ResponseEntity<Boolean> existsByItemIdAndIsGloballyFeatured(@PathVariable Long itemId) {
        return ResponseEntity.ok(collectionService.isItemInAnyGloballyFeaturedCollection(itemId));
    }

    @PostMapping
    public ResponseEntity<CollectionDTO> addReadingList(@RequestBody CreateCollectionDTO collectionDTO) {
        return ResponseEntity.ok(collectionService.addCollection(collectionDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionDTO> updateReadingList(@RequestBody CreateCollectionDTO collectionDTO, @PathVariable Long id) {
        return ResponseEntity.ok(collectionService.updateCollection(collectionDTO, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CollectionDTO> deleteReadingList(@PathVariable Long id) {
        return ResponseEntity.ok(collectionService.deleteCollection(id));
    }
}
