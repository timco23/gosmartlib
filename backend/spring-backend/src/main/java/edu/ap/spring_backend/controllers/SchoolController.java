package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.service.SchoolService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/schools")
public class SchoolController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
    public List<School> getAllSchools() {
        return schoolService.getAllSchools();
    }

    @PostMapping
    public School createSchool(@RequestBody School school) {
        return schoolService.createSchool(school);
    }

    @DeleteMapping("/{id}")
    public void deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
    }

    @PutMapping("/{id}")
    public School updateSchool(@PathVariable Long id, @RequestBody School school) {
        return schoolService.updateSchool(id, school);
    }
}