package edu.ap.spring_backend.service;

import java.util.List;

import edu.ap.spring_backend.dao.CampusRepository;
import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.CreateSchoolItemCopyDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Campus;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.School;
import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dto.SchoolItemCopyDTO;
import edu.ap.spring_backend.jpa.SchoolItemCopy;

@Service
public class SchoolItemCopyService {
    private final SchoolItemCopyRepository schoolItemCopyRepository;
    private final ItemRepository itemRepository;
    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;

    public SchoolItemCopyService(SchoolItemCopyRepository schoolItemCopyRepository,  ItemRepository itemRepository,  SchoolRepository schoolRepository, CampusRepository campusRepository) {
        this.schoolItemCopyRepository = schoolItemCopyRepository;
        this.itemRepository = itemRepository;
        this.schoolRepository = schoolRepository;
        this.campusRepository = campusRepository;
    }

    public List<SchoolItemCopy> getCopiesByItemId(int itemId) {
        return schoolItemCopyRepository.findByItemId(itemId);
    }

    public SchoolItemCopy addCopy(CreateSchoolItemCopyDTO copyDTO) {
        SchoolItemCopy copy = new SchoolItemCopy();
        Item item = itemRepository.findById(copyDTO.itemId()).orElseThrow(EntityNotFoundException::new);
        School school = schoolRepository.findById(copyDTO.schoolId()).orElseThrow(EntityNotFoundException::new);
        Campus campus = campusRepository.findById(copyDTO.campusId()).orElse(null);
        copy.setItem(item);
        copy.setSchool(school);
        copy.setCampus(campus);
        copy.setBarcode(copyDTO.barcode());
        copy.setFeatured(copyDTO.featured());
        copy.setCopy_status(copyDTO.copy_status());
        copy.setCondition(copyDTO.condition());
        return schoolItemCopyRepository.save(copy);
    }

    public SchoolItemCopy deleteCopy(int id) {
        SchoolItemCopy copy = schoolItemCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaar niet gevonden"));
        schoolItemCopyRepository.delete(copy);
        return copy;
    }

    public SchoolItemCopy updateCopy(int id, CreateSchoolItemCopyDTO copyDTO) {
        SchoolItemCopy copy = schoolItemCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaar niet gevonden"));
        Campus campus = campusRepository.findById(copyDTO.campusId()).orElse(null);
        copy.setCampus(campus);
        copy.setCondition(copyDTO.condition());
        copy.setBarcode(copyDTO.barcode());
        copy.setCopy_status(copyDTO.copy_status());
        schoolItemCopyRepository.save(copy);
        if (copy.isFeatured() != copyDTO.featured()) {
            copy.setFeatured(copyDTO.featured());
            List<SchoolItemCopy> schoolItemCopies = getCopiesByItemId(copyDTO.itemId());
            schoolItemCopies.forEach(schoolItemCopy -> {
                schoolItemCopy.setFeatured(copyDTO.featured());
                schoolItemCopyRepository.save(schoolItemCopy);
            });
        }
        return copy;
    }
}
