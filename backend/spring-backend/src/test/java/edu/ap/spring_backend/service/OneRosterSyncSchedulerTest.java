package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ClassRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.SchoolClass;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OneRosterSyncSchedulerTest {

    @Mock
    private OneRosterOAuthService oneRosterOAuthService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private OneRosterSyncScheduler oneRosterSyncScheduler;

    private User testUser;
    private School testSchool;
    private SchoolClass testClass;

    @BeforeEach
    void setUp() throws Exception {
        testSchool = new School();
        testSchool.setId(1L);
        testSchool.setName("Test School");

        testUser = buildUser(1, "jan@student.be", Role.LEERLING);
        testUser.setFirstName("Jan");
        testUser.setName("Peeters");
        testUser.setSchool(testSchool);

        testClass = new SchoolClass("Class A", testSchool, new ArrayList<>());
        testClass.setId(10L);
    }

    private User buildUser(Integer id, String email, Role role) throws Exception {
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setClass(new ArrayList<>());
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
        return user;
    }

    @Test
    void givenUserAndOneRosterData_whenSyncAll_thenMetadataAndClassesUpdated() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(schoolRepository.findAll()).thenReturn(List.of(testSchool));

        // Mock OneRoster User (Phase 1)
        OneRosterSchoolDTO orSchool = new OneRosterSchoolDTO("s1", "active", "Test School", "school", "123");
        OneRosterUserDTO orUser = new OneRosterUserDTO("or-id-123", null, "active", "janp", "Jan", "Peeters", "jan@student.be", "student", null, new OneRosterSchoolDTO[]{orSchool});
        
        when(oneRosterOAuthService.getAllStudents()).thenReturn(new OneRosterUserResponseDTO(new OneRosterUserDTO[]{orUser}));
        when(oneRosterOAuthService.getAllTeachers()).thenReturn(new OneRosterUserResponseDTO(new OneRosterUserDTO[]{}));

        // Mock Enrollments (Phase 2)
        OneRosterClassDTO classDTO = new OneRosterClassDTO("c1", "active", "Class A", "A", "type", null, "type");
        OneRosterEnrollmentDTO enrollment = new OneRosterEnrollmentDTO("e1", "active", orUser, "student", classDTO, null, "true");
        
        when(oneRosterOAuthService.getEnrollmentsForSchool("or-id-123"))
                .thenReturn(new OneRosterEnrollmentsResponseDTO(new OneRosterEnrollmentDTO[]{enrollment}));
        
        when(classRepository.findByClassNameAndSchoolId("Class A", 1L)).thenReturn(Optional.of(testClass));

        // Act
        oneRosterSyncScheduler.syncAll();

        // Assert Phase 1
        assertEquals("or-id-123", testUser.getOnerosterId());
        
        // Assert Phase 2
        assertTrue(testUser.getClasses().contains(testClass));
        assertTrue(testClass.getUsers().contains(testUser));
        
        verify(userRepository, atLeastOnce()).save(testUser);
        verify(classRepository, atLeastOnce()).save(testClass);
    }

    @Test
    void givenUserWithWrongSchool_whenSyncAll_thenSchoolUpdated() {
        // Arrange
        School otherSchool = new School();
        otherSchool.setId(2L);
        otherSchool.setName("Other School");
        
        testUser.setSchool(otherSchool);
        
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(schoolRepository.findAll()).thenReturn(List.of(testSchool, otherSchool));

        OneRosterSchoolDTO orSchool = new OneRosterSchoolDTO("s1", "active", "Test School", "school", "123");
        OneRosterUserDTO orUser = new OneRosterUserDTO("or-id-123", null, "active", "janp", "Jan", "Peeters", "jan@student.be", "student", null, new OneRosterSchoolDTO[]{orSchool});
        
        when(oneRosterOAuthService.getAllStudents()).thenReturn(new OneRosterUserResponseDTO(new OneRosterUserDTO[]{orUser}));
        when(oneRosterOAuthService.getAllTeachers()).thenReturn(new OneRosterUserResponseDTO(new OneRosterUserDTO[]{}));
        
        // Mock enrollment for the new school
        when(oneRosterOAuthService.getEnrollmentsForSchool("or-id-123"))
                .thenReturn(new OneRosterEnrollmentsResponseDTO(new OneRosterEnrollmentDTO[]{}));

        // Act
        oneRosterSyncScheduler.syncAll();

        // Assert
        assertEquals(testSchool, testUser.getSchool());
        verify(userRepository, atLeastOnce()).save(testUser);
    }

    @Test
    void givenUserInObsoleteClass_whenSyncAll_thenUserRemovedFromClass() {
        // Arrange
        testUser.setOnerosterId("or-id-123");
        testUser.getClasses().add(testClass);
        testClass.getUsers().add(testUser);

        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(schoolRepository.findAll()).thenReturn(List.of(testSchool));
        
        // No users in OneRoster phase 1
        when(oneRosterOAuthService.getAllStudents()).thenReturn(new OneRosterUserResponseDTO(new OneRosterUserDTO[]{}));
        when(oneRosterOAuthService.getAllTeachers()).thenReturn(new OneRosterUserResponseDTO(new OneRosterUserDTO[]{}));

        when(oneRosterOAuthService.getEnrollmentsForSchool("or-id-123"))
                .thenReturn(new OneRosterEnrollmentsResponseDTO(new OneRosterEnrollmentDTO[]{}));

        // Act
        oneRosterSyncScheduler.syncAll();

        // Assert
        assertFalse(testUser.getClasses().contains(testClass));
        assertFalse(testClass.getUsers().contains(testUser));
        verify(classRepository).save(testClass);
    }

    @Test
    void givenEmptyClasses_whenSyncAll_thenClassesDeleted() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(classRepository.findEmptyClasses()).thenReturn(List.of(testClass));

        // Act
        oneRosterSyncScheduler.syncAll();

        // Assert
        verify(classRepository).deleteAll(List.of(testClass));
    }

    @Test
    void givenMultipleOneRosterMatchesByName_whenSyncAll_thenDisambiguateByEmail() throws Exception {
        // Arrange
        User user1 = buildUser(2, "jan1@school.be", Role.LEERLING);
        user1.setFirstName("Jan"); user1.setName("Peeters");
        user1.setSchool(testSchool);
        
        when(userRepository.findAll()).thenReturn(List.of(user1));
        when(schoolRepository.findAll()).thenReturn(List.of(testSchool));

        // Two OneRoster users with same name
        OneRosterSchoolDTO orSchool = new OneRosterSchoolDTO("s1", "active", "Test School", "school", "123");
        OneRosterUserDTO or1 = new OneRosterUserDTO("or-1", null, null, null, "Jan", "Peeters", "jan1@school.be", null, null, new OneRosterSchoolDTO[]{orSchool});
        OneRosterUserDTO or2 = new OneRosterUserDTO("or-2", null, null, null, "Jan", "Peeters", "jan2@school.be", null, null, new OneRosterSchoolDTO[]{orSchool});
        
        when(oneRosterOAuthService.getAllStudents()).thenReturn(new OneRosterUserResponseDTO(new OneRosterUserDTO[]{or1, or2}));
        when(oneRosterOAuthService.getAllTeachers()).thenReturn(new OneRosterUserResponseDTO(new OneRosterUserDTO[]{}));
        
        when(oneRosterOAuthService.getEnrollmentsForSchool("or-1")).thenReturn(new OneRosterEnrollmentsResponseDTO(new OneRosterEnrollmentDTO[]{}));

        // Act
        oneRosterSyncScheduler.syncAll();

        // Assert
        assertEquals("or-1", user1.getOnerosterId());
    }
}
