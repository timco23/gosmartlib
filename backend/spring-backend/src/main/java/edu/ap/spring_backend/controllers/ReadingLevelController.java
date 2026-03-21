package edu.ap.spring_backend.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ap.spring_backend.jpa.ReadingLevel;
import edu.ap.spring_backend.service.ReadingLevelService;

@RestController
@RequestMapping("/readinglevels")
public class ReadingLevelController {
    private final ReadingLevelService readingLevelService;

    public ReadingLevelController(ReadingLevelService service){
        readingLevelService = service;
    }

    @GetMapping()
    public List<ReadingLevel> GetAll(){
        return readingLevelService.GetAllReadingLevels();
    }
}
