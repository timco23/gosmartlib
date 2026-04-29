package edu.ap.spring_backend.service;

import java.util.List;

import edu.ap.spring_backend.dao.CampusRepository;
import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.CreateSchoolItemCopyDTO;
import edu.ap.spring_backend.dto.SchoolItemCopyDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Campus;
import edu.ap.spring_backend.jpa.CopyStatus;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.ItemCondition;
import edu.ap.spring_backend.jpa.School;
import org.springframework.stereotype.Service;
import edu.ap.spring_backend.dao.LendingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.jpa.SchoolItemCopy;

@Service
public class SchoolItemCopyService {
    private final SchoolItemCopyRepository schoolItemCopyRepository;
    private final ItemRepository itemRepository;
    private final SchoolRepository schoolRepository;
    private final CampusRepository campusRepository;
    private final LendingRepository lendingRepository;

    public SchoolItemCopyService(SchoolItemCopyRepository schoolItemCopyRepository,  ItemRepository itemRepository,  SchoolRepository schoolRepository, CampusRepository campusRepository,  LendingRepository lendingRepository) {
        this.schoolItemCopyRepository = schoolItemCopyRepository;
        this.itemRepository = itemRepository;
        this.schoolRepository = schoolRepository;
        this.campusRepository = campusRepository;
        this.lendingRepository = lendingRepository;
    }

    public List<SchoolItemCopyDTO> getCopiesByIds(List<Integer> ids) {
        return schoolItemCopyRepository.findAllById(ids)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<SchoolItemCopyDTO> getCopiesByItemIdAndSchoolId(Long itemId, Long schoolId) {
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
        copy.setFeatured(copyDTO.featured());
        copy.setCopy_status(copyDTO.copy_status());
        copy.setCondition(copyDTO.condition());
        SchoolItemCopy saved = schoolItemCopyRepository.save(copy);
        saved.setBarcode(String.format("BC%06d", saved.getId()));
        return mapToDTO(schoolItemCopyRepository.save(saved));
    }

    public List<SchoolItemCopyDTO> addCopies(List<CreateSchoolItemCopyDTO> copyDTOs) {
        return copyDTOs.stream()
                .map(this::addCopy)
                .toList();
    }

     public SchoolItemCopyDTO deleteCopy(int id) {
        SchoolItemCopyDTO copy = getCopyById(id); 

        if (lendingRepository.existsByBook_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exemplaar kan niet verwijderd worden: er bestaat uitleengeschiedenis."
            );
        }

        try {
            schoolItemCopyRepository.deleteById(id);
            return copy;
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exemplaar kan niet verwijderd worden door gekoppelde gegevens."
            );
        }
    }

    public SchoolItemCopyDTO updateCopy(int id, CreateSchoolItemCopyDTO copyDTO) {
        SchoolItemCopy copy = schoolItemCopyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaar niet gevonden"));
        if (copyDTO.campusId() != null) {
            Campus campus = campusRepository.findById(copyDTO.campusId()).orElse(null);
            copy.setCampus(campus);
        }
        copy.setCondition(copyDTO.condition());
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

    public SchoolItemCopyDTO getCopyByBarcode(String barcode) {
        SchoolItemCopy copy = schoolItemCopyRepository.findByBarcode(barcode).orElse(null);
        if (copy != null) {
            return mapToDTO(copy);
        }
        return null;
    }

    public SchoolItemCopyDTO getCopyById(int copyId) {
        SchoolItemCopy copy = schoolItemCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("Exemplaar niet gevonden"));
        return mapToDTO(copy);
    }

    public SchoolItemCopyDTO updateCopyStatus(int copyId, String status) throws EntityNotFoundException {
        SchoolItemCopy copy = schoolItemCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("Exemplaar niet gevonden"));
        copy.setCopy_status(CopyStatus.valueOf(status));
        schoolItemCopyRepository.save(copy);
        return mapToDTO(copy);
    }

    public SchoolItemCopyDTO updateCopyCondition(int copyId, String condition) throws EntityNotFoundException {
        SchoolItemCopy copy = schoolItemCopyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("Exemplaar niet gevonden"));
        copy.setCondition(ItemCondition.valueOf(condition));
        schoolItemCopyRepository.save(copy);
        return mapToDTO(copy);
    }
}