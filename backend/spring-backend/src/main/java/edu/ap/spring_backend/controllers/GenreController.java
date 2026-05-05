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

import edu.ap.spring_backend.dto.GenreWithSubgenresDTO;
import edu.ap.spring_backend.jpa.Genre;
import edu.ap.spring_backend.service.GenreService;

@RestController
@RequestMapping("/genres")
public class GenreController {
    GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public ResponseEntity<List<GenreWithSubgenresDTO>> getAll() {
        return ResponseEntity.ok(genreService.getAll());
    }

    @PostMapping
    public ResponseEntity<Genre> addNewGenre(@RequestBody Genre newGenre) {
        return ResponseEntity.ok(genreService.addGenre(newGenre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Genre> updateGenre(@PathVariable Long id, @RequestBody Genre updatedGenre) {
        return ResponseEntity.ok(genreService.updateGenre(id, updatedGenre));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}