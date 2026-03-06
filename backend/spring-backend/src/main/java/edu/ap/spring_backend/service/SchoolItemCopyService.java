package edu.ap.spring_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dto.SchoolItemCopyDTO;
import edu.ap.spring_backend.jpa.SchoolItemCopy;

@Service
public class SchoolItemCopyService {
    private final SchoolItemCopyRepository schoolItemCopyRepository;

    public SchoolItemCopyService(SchoolItemCopyRepository schoolItemCopyRepository) {
        this.schoolItemCopyRepository = schoolItemCopyRepository;
    }

    public List<SchoolItemCopyDTO> getCopiesBySchoolItemId(int schoolItemId) {
        return schoolItemCopyRepository.findBySchoolItemId(schoolItemId);
    }
}
