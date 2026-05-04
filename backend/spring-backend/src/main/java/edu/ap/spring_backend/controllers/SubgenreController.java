package edu.ap.spring_backend.controllers;

import java.util.List;

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
    public List<Subgenre> getAllSubgenres() {
        return subgenreService.getAll();
    }

    @PostMapping()
    public Subgenre create(@RequestBody SubgenreDTO subgenre) {
        return subgenreService.create(subgenre);
    }

    @GetMapping("/genre")
    public List<Subgenre> getSubgenresByGenres(@RequestParam List<Long> genreIds) {
        return subgenreService.getFromGenreIds(genreIds);
    }

    @PutMapping("/{id}")
    public Subgenre updateSubgenre(@PathVariable Long id, @RequestBody Subgenre updatedSubgenre) {
        return subgenreService.updateSubgenre(id, updatedSubgenre);
    }

    @DeleteMapping("/{id}")
    public void deleteSubgenre(@PathVariable Long id) {
        subgenreService.deleteSubgenre(id);
    }

}
