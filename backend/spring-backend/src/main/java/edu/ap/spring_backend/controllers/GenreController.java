package edu.ap.spring_backend.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<Genre> getAll() {
        return genreService.getAll();
    }
}
