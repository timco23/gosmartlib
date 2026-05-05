package edu.ap.spring_backend.controllers;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.ap.spring_backend.dto.SubgenreDTO;
import edu.ap.spring_backend.jpa.Subgenre;
import edu.ap.spring_backend.service.SubgenreService;

@RestController
@RequestMapping("/subgenres")
public class SubgenreController {

    private final SubgenreService subgenreService;

    public SubgenreController(SubgenreService service) {
        subgenreService = service;
    }

    @GetMapping()
    public ResponseEntity<List<Subgenre>> getAllSubgenres() {
        return ResponseEntity.ok(subgenreService.getAll());
    }

    @PostMapping()
    public ResponseEntity<Subgenre> create(@RequestBody SubgenreDTO subgenre) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subgenreService.create(subgenre));
    }

    @GetMapping("/genre")
    public ResponseEntity<List<SubgenreDTO>> getSubgenresByGenres(@RequestParam List<Long> genreIds) {
        return ResponseEntity.ok(subgenreService.getFromGenreIds(genreIds));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subgenre> updateSubgenre(@PathVariable Long id, @RequestBody Subgenre updatedSubgenre) {
        return ResponseEntity.ok(subgenreService.updateSubgenre(id, updatedSubgenre));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubgenre(@PathVariable Long id) {
        subgenreService.deleteSubgenre(id);
        return ResponseEntity.noContent().build();
    }
}