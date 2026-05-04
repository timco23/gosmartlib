package edu.ap.spring_backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ap.spring_backend.dto.DidacticCategoryDTO;
import edu.ap.spring_backend.jpa.DidacticCategory;
import edu.ap.spring_backend.service.DidacticCategoryService;

@RestController
@RequestMapping("/didacticcategories")
public class DidacticCategoryController {
    private final DidacticCategoryService didacticCategoryService;

    public DidacticCategoryController(DidacticCategoryService service){
        didacticCategoryService = service;
    }

    @GetMapping()
    public List<DidacticCategory> GetAll(){
        return didacticCategoryService.getAll();
    }

    @PostMapping()
    public ResponseEntity<DidacticCategory> addDidacticCategory(@RequestBody DidacticCategoryDTO didacticCategoryDTO) {
        return ResponseEntity.ok(didacticCategoryService.addDidacticCategory(didacticCategoryDTO));
    }

    /**
     * Updates an existing didactic category with the given ID using the provided DTO.
     * @param id the ID of the didactic category to update
     * @param dto the data transfer object containing the updated fields for the didactic category
     * @return ResponseEntity containing the updated DidacticCategory and HTTP status 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<DidacticCategory> updateDidacticCategory(
            @PathVariable Long id,
            @RequestBody DidacticCategoryDTO dto) {
        DidacticCategory updated = didacticCategoryService.updateDidacticCategory(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDidacticCategory(@PathVariable Long id) {
        didacticCategoryService.deleteDidacticCategory(id);
        return ResponseEntity.noContent().build();
    }
}
