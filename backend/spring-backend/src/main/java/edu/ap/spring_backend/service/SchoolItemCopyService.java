package edu.ap.spring_backend.service;

import java.util.List;

import edu.ap.spring_backend.dao.CampusRepository;
import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.CreateSchoolItemCopyDTO;
import edu.ap.spring_backend.dto.SchoolItemCopyDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Campus;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.School;
import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
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

    public List<SchoolItemCopyDTO> getCopiesByItemIdAndSchoolId(int itemId, Long schoolId) {
        return schoolItemCopyRepository.findByItemIdAndSchoolId(itemId, schoolId)
                .stream()
                .map((copy) -> mapToDTO(copy)).toList();
    }

    public SchoolItemCopyDTO addCopy(CreateSchoolItemCopyDTO copyDTO) {
        SchoolItemCopy copy = new SchoolItemCopy();
        Item item = itemRepository.findById(copyDTO.itemId()).orElseThrow(EntityNotFoundException::new);
        School school = schoolRepository.findById(copyDTO.schoolId()).orElseThrow(EntityNotFoundException::new);
        if (copyDTO.campusId() != null) {
            Campus campus = campusRepository.findById(copyDTO.campusId()).orElse(null);
            copy.setCampus(campus);
        }
        copy.setItem(item);
        copy.setSchool(school);
        copy.setBarcode(copyDTO.barcode());
        copy.setFeatured(copyDTO.featured());
        copy.setCopy_status(copyDTO.copy_status());
        copy.setCondition(copyDTO.condition());
        return mapToDTO(schoolItemCopyRepository.save(copy));
    }

    public SchoolItemCopyDTO deleteCopy(int id) {
        SchoolItemCopy copy = schoolItemCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaar niet gevonden"));
        schoolItemCopyRepository.delete(copy);
        return mapToDTO(copy);
    }

    public SchoolItemCopyDTO updateCopy(int id, CreateSchoolItemCopyDTO copyDTO) {
        SchoolItemCopy copy = schoolItemCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaar niet gevonden"));
        if (copyDTO.campusId() != null) {
            Campus campus = campusRepository.findById(copyDTO.campusId()).orElse(null);
            copy.setCampus(campus);
        }
        copy.setCondition(copyDTO.condition());
        copy.setBarcode(copyDTO.barcode());
        copy.setCopy_status(copyDTO.copy_status());
        schoolItemCopyRepository.save(copy);
        if (copy.isFeatured() != copyDTO.featured()) {
            copy.setFeatured(copyDTO.featured());
            List<SchoolItemCopy> schoolItemCopies = schoolItemCopyRepository.findByItemIdAndSchoolId(copyDTO.itemId(), copyDTO.schoolId());
            schoolItemCopies.forEach(schoolItemCopy -> {
                schoolItemCopy.setFeatured(copyDTO.featured());
                schoolItemCopyRepository.save(schoolItemCopy);
            });
        }
        return mapToDTO(copy);
    }

    public SchoolItemCopyDTO getByItemTitleAndSchoolId(String title, Long schoolId) {
        SchoolItemCopy copy = schoolItemCopyRepository.findByItemTitleAndSchoolId(title, schoolId).orElse(null);
        if (copy != null) {
            return mapToDTO(copy);
        }
        return null;
    }

    public SchoolItemCopyDTO mapToDTO(SchoolItemCopy copy) {
        return new SchoolItemCopyDTO(
                copy.getId(),
                copy.getItem().getId(),
                copy.getBarcode(),
                copy.getCondition(),
                copy.getCopy_status(),
                copy.isFeatured(),
                copy.getSchool().getId(),
                copy.getCampus()
        );
    }
}
