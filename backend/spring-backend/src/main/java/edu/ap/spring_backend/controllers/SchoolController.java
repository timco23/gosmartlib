package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.entity.School;
import edu.ap.spring_backend.service.SchoolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

@RestController
@RequestMapping("/schools")
public class SchoolController {

    private final SchoolService schoolService;
    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();


    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
    public List<School> getAllSchools() {
        return schoolService.getAllSchools();
    }
    
    @PostMapping
    public School createSchool(@RequestBody String school) {
        School schoolObject = gson.fromJson(school, School.class);
        return schoolService.createSchool(schoolObject);
    }
}