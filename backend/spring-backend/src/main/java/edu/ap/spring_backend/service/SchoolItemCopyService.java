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

    public List<SchoolItemCopy> getCopiesBySchoolItemId(int schoolItemId) {
        return schoolItemCopyRepository.findBySchoolItemId(schoolItemId);
    }

    public SchoolItemCopy addCopy(SchoolItemCopyDTO copyDTO) {
        SchoolItemCopy copy = new SchoolItemCopy();
        copy.setSchoolItemId(copyDTO.schoolItemId());
        copy.setCondition(copyDTO.condition());
        copy.setBarcode(copyDTO.barcode());
        copy.setCopy_status(copyDTO.copy_status());
        return schoolItemCopyRepository.save(copy);
    }

    public SchoolItemCopy deleteCopy(int id) {
        SchoolItemCopy copy = schoolItemCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaar niet gevonden"));
        schoolItemCopyRepository.delete(copy);
        return copy;
    }

    public SchoolItemCopy updateCopy(int id, SchoolItemCopyDTO copyDTO) {
        SchoolItemCopy copy = schoolItemCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaar niet gevonden"));
        copy.setCondition(copyDTO.condition());
        copy.setBarcode(copyDTO.barcode());
        copy.setCopy_status(copyDTO.copy_status());
        return schoolItemCopyRepository.save(copy);
    }
}
