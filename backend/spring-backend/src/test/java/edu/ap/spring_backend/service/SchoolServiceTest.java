package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.jpa.Campus;
import edu.ap.spring_backend.jpa.School;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchoolServiceTest {

    @Mock private SchoolRepository schoolRepository;

    @InjectMocks
    private SchoolService service;

    private School existing;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existing = new School();
        existing.setId(1L);
        existing.setName("Old");
        existing.setAddress("Old Address");
        existing.setCampuses(new ArrayList<>());
    }

    @Test
    void getAllSchools_returnsAll() {
        when(schoolRepository.findAll()).thenReturn(List.of(existing));

        List<School> result = service.getAllSchools();

        assertEquals(1, result.size());
    }

    @Test
    void createSchool_saves() {
        when(schoolRepository.save(existing)).thenReturn(existing);

        School result = service.createSchool(existing);

        assertEquals("Old", result.getName());
    }

    @Test
    void deleteSchool_deletesById() {
        service.deleteSchool(1L);
        verify(schoolRepository).deleteById(1L);
    }

    @Test
    void getSchoolById_returnsSchool() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(existing));

        School result = service.getSchoolById(1L);

        assertEquals("Old", result.getName());
    }

    @Test
    void getSchoolByName_returnsSchool() {
        when(schoolRepository.findByName("Old")).thenReturn(Optional.of(existing));

        School result = service.getSchoolByName("Old");

        assertEquals("Old", result.getName());
    }

    @Test
    void updateSchoolSettings_updatesFields() {
        School updated = new School();
        updated.setLendingPeriodDays(14);
        updated.setExtendPeriodDays(7);
        updated.setMaxExtends(2);
        updated.setMaxLendsStudent(5);
        updated.setMaxLendTeacher(10);
        updated.setReviews(true);
        updated.setReviewModeration(true);

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        School result = service.updateSchoolSettings(1L, updated);

        assertEquals(14, result.getLendingPeriodDays());
        assertTrue(result.isReviews());
    }

    @Test
    void updateSchool_updatesBasics() {
        School updated = new School();
        updated.setName("New");
        updated.setAddress("New Address");
        updated.setCampuses(List.of(new Campus()));

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        School result = service.updateSchool(1L, updated);

        assertEquals("New", result.getName());
        assertEquals("New Address", result.getAddress());
    }
}