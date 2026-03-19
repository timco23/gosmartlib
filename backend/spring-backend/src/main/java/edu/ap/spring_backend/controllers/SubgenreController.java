package edu.ap.spring_backend.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ap.spring_backend.dto.SubgenreDTO;
import edu.ap.spring_backend.jpa.Subgenre;
import edu.ap.spring_backend.service.SubgenreService;

@RestController
@RequestMapping("/subgenres")
public class SubgenreController {
    private final SubgenreService subgenreService;

    public SubgenreController(SubgenreService service){
        subgenreService = service;
    }

    @GetMapping()
    public List<Subgenre> GetAllSubgenres(){
        return subgenreService.GetAll();
    }

    @PostMapping()
    public Subgenre create(@RequestBody SubgenreDTO subgenre) {
        return subgenreService.create(subgenre);
    }

    @GetMapping()
        public List<Subgenre> getSubgenresByGenre(@PathVariable long genreId) {
    return subgenreService.getFromGenreId(genreId);
}

}
