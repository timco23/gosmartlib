package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.ClassDTO;
import edu.ap.spring_backend.dto.UserResponse;
import edu.ap.spring_backend.service.ClassService;
import edu.ap.spring_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/classes")
public class SchoolClassController {
    private final ClassService classService;

    public SchoolClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public ResponseEntity<List<ClassDTO>> getClassesBySchoolId(@RequestParam Long schoolId) {
        return ResponseEntity.ok(classService.getClassesBySchoolId(schoolId));
    }
}
