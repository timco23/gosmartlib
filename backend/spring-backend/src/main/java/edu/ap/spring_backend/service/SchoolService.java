package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.exception.EntityNotFoundException;
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

    private School findOrThrow(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    private void applySettings(School existing, School updated) {
        existing.setLendingPeriodDays(updated.getLendingPeriodDays());
        existing.setExtendPeriodDays(updated.getExtendPeriodDays());
        existing.setMaxExtends(updated.getMaxExtends());
        existing.setMaxLendsStudent(updated.getMaxLendsStudent());
        existing.setMaxLendTeacher(updated.getMaxLendTeacher());
        existing.setLenderNotificationPeriod(updated.getLenderNotificationPeriod());
        existing.setLenderLateNotificationPeriod(updated.getLenderLateNotificationPeriod());
        existing.setLibrarianNotificationNotReturned(updated.getLibrarianNotificationNotReturned());
        existing.setDashboardShowBookOfMonth(updated.isDashboardShowBookOfMonth());
        existing.setDashboardShowLastBorrowed(updated.isDashboardShowLastBorrowed());
        existing.setDashboardShowAnnouncement(updated.isDashboardShowAnnouncement());
        existing.setDashboardAnnouncement(updated.getDashboardAnnouncement());
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
        School existingSchool = findOrThrow(id);

        existingSchool.setName(updatedSchool.getName());
        existingSchool.setAddress(updatedSchool.getAddress());
        applySettings(existingSchool, updatedSchool);

        existingSchool.getCampuses().clear();
        for (Campus campus : updatedSchool.getCampuses()) {
            existingSchool.addCampus(new Campus(campus.getName(), existingSchool));
        }

        return schoolRepository.save(existingSchool);
    }

    public School getSchoolByName(String name) {
        return schoolRepository.findByName(name)
                .orElseThrow(EntityNotFoundException::new);
    }

    public School getSchoolById(Long id) {
        return findOrThrow(id);
    }

    public School updateSchoolSettings(Long id, School updatedSchool) {
        School existingSchool = findOrThrow(id);
        applySettings(existingSchool, updatedSchool);
        return schoolRepository.save(existingSchool);
    }
}