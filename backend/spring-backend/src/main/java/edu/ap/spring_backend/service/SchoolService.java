// ...existing code...
package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.Campus;
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

    public School createSchool(School school) {
        return schoolRepository.save(school);
    }

    public void deleteSchool(Long id) {
        schoolRepository.deleteById(id);
    }

    public School updateSchool(Long id, School updatedSchool) {
        School existingSchool = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found with id " + id));

        existingSchool.setName(updatedSchool.getName());
        existingSchool.setAddress(updatedSchool.getAddress());
        existingSchool.setLendingPeriodDays(updatedSchool.getLendingPeriodDays());
        existingSchool.setExtendPeriodDays(updatedSchool.getExtendPeriodDays());
        existingSchool.setMaxExtends(updatedSchool.getMaxExtends());
        existingSchool.setMaxLendsStudent(updatedSchool.getMaxLendsStudent());
        existingSchool.setMaxLendTeacher(updatedSchool.getMaxLendTeacher());
        existingSchool.setLenderNotificationPeriod(updatedSchool.getLenderNotificationPeriod());
        existingSchool.setLenderLateNotificationPeriod(updatedSchool.getLenderLateNotificationPeriod());
        existingSchool.setLibrarianNotificationNotReturned(updatedSchool.getLibrarianNotificationNotReturned());
        existingSchool.setLibrarianRequestNotification(updatedSchool.getLibrarianRequestNotification());
        existingSchool.setExternalVisibility(updatedSchool.isExternalVisibility());
        existingSchool.setExternalLending(updatedSchool.isExternalLending());
        existingSchool.setReviews(updatedSchool.isReviews());
        existingSchool.setReviewModeration(updatedSchool.isReviewModeration());

        // Update campussen
        existingSchool.getCampuses().clear();
        for (Campus campus : updatedSchool.getCampuses()) {
            existingSchool.addCampus(new Campus(campus.getName(), existingSchool));
        }

        return schoolRepository.save(existingSchool); 
    }

    public School getSchoolByName(String name) {
        School school = schoolRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("School not found with name " + name));
        return school;
    }

    public School getSchoolById(Long id) {
        School school =  schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found with id " + id));
        return school;
    }
}