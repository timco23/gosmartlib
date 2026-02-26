// ...existing code...
package edu.ap.spring_backend.service;

import edu.ap.spring_backend.entity.School;
import edu.ap.spring_backend.DAO.SchoolRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SchoolService {
    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }
}