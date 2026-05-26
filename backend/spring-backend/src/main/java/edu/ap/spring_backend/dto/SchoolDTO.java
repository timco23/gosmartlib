package edu.ap.spring_backend.dto;

import edu.ap.spring_backend.jpa.Campus;
import edu.ap.spring_backend.jpa.School;
import org.springframework.beans.BeanUtils;

import java.util.List;

public record SchoolDTO(
        Long id,
        String name,
        String address,
        int lendingPeriodDays,
        int extendPeriodDays,
        int maxExtends,
        int maxLendsStudent,
        int maxLendTeacher,
        int lenderNotificationPeriod,
        int lenderLateNotificationPeriod,
        int librarianNotificationNotReturned,
        boolean librarianRequestNotification,
        boolean externalVisibility,
        boolean externalLending,
        boolean reviews,
        boolean reviewModeration,
        boolean dashboardShowBookOfMonth,
        boolean dashboardShowLastBorrowed,
        boolean dashboardShowAnnouncement,
        String dashboardAnnouncement,
        List<CampusDTO> campuses
) {
    public record CampusDTO(Long id, String name) {}

    public static SchoolDTO from(School school) {
        List<CampusDTO> campusDTOs = school.getCampuses().stream()
                .map(c -> new CampusDTO(c.getId(), c.getName()))
                .toList();

        return new SchoolDTO(
                school.getId(),
                school.getName(),
                school.getAddress(),
                school.getLendingPeriodDays(),
                school.getExtendPeriodDays(),
                school.getMaxExtends(),
                school.getMaxLendsStudent(),
                school.getMaxLendTeacher(),
                school.getLenderNotificationPeriod(),
                school.getLenderLateNotificationPeriod(),
                school.getLibrarianNotificationNotReturned(),
                school.isLibrarianRequestNotification(),
                school.isExternalVisibility(),
                school.isExternalLending(),
                school.isReviews(),
                school.isReviewModeration(),
                school.isDashboardShowBookOfMonth(),
                school.isDashboardShowLastBorrowed(),
                school.isDashboardShowAnnouncement(),
                school.getDashboardAnnouncement(),
                campusDTOs
        );
    }

    public School toEntity() {
        School school = new School();
        BeanUtils.copyProperties(this, school);
        school.getCampuses().clear();
        if (campuses != null) {
            for (CampusDTO campusDTO : campuses) {
                Campus campus = new Campus();
                campus.setName(campusDTO.name());
                if (campusDTO.id() != null) campus.setId(campusDTO.id());
                school.addCampus(campus);
            }
        }
        return school;
    }
}
