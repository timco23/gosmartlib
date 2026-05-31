package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.SchoolClass;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.service.ClassService;
import edu.ap.spring_backend.service.OneRosterOAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = SmartschoolOAuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class,
                Saml2RelyingPartyAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "smartschool.client-id=test-client-id",
        "smartschool.client-secret=test-secret",
        "smartschool.platform=test.smartschool.be",
        "smartschool.redirect-uri=http://localhost/oauth/smartschool/callback",
        "app.frontend-base-url=http://localhost:3000",
        "app.jwt.secret=testsecret12345678901234567890ab",
        "app.jwt.expires-minutes=60"
})
class SmartschoolClassSyncTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchoolRepository schoolRepository;

    @MockBean
    private ClassService classService;

    @MockBean
    private OneRosterOAuthService oneRosterOAuthService;

    @Autowired
    private SmartschoolOAuthController controller;

    private RestTemplate mockRestTemplate;
    private School defaultSchool;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        mockRestTemplate = Mockito.mock(RestTemplate.class);
        Field field = SmartschoolOAuthController.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(controller, mockRestTemplate);

        Mockito.lenient().when(jwtService.generateToken(any())).thenReturn("fake-jwt");

        defaultSchool = new School();
        defaultSchool.setId(1L);
        defaultSchool.setName("Test School");
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(defaultSchool));
        when(schoolRepository.findAll()).thenReturn(List.of(defaultSchool));

        when(mockRestTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(Map.of("access_token", "fake-token"), HttpStatus.OK));

        stubOneRoster("Test", "User", "Test School");
    }

    private void stubOneRoster(String firstName, String familyName, String schoolName) {
        OneRosterSchoolDTO school = new OneRosterSchoolDTO("school-1", "active", schoolName, "school", "123");
        OneRosterUserDTO orUser = new OneRosterUserDTO("or-id-123", "id-123", "active", "username", firstName, familyName, "email", "student", "male", new OneRosterSchoolDTO[]{school});

        when(oneRosterOAuthService.getAllStudents()).thenReturn(new OneRosterUserResponseDTO(new OneRosterUserDTO[]{orUser}));
        when(oneRosterOAuthService.getStudentById("or-id-123")).thenReturn(new OneRosterSingleUserResponseDTO(orUser));
        when(oneRosterOAuthService.getEnrollmentsForSchool(anyString())).thenReturn(new OneRosterEnrollmentsResponseDTO(new OneRosterEnrollmentDTO[0]));
    }

    private User buildUser(Integer id, String email, Role role) throws Exception {
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
        return user;
    }

    private SmartschoolUserInfo buildUserInfo(String username, String email, String basisrol) {
        SmartschoolUserInfo info = new SmartschoolUserInfo();
        info.setUsername(username);
        info.setName("Test");
        info.setSurname("User");
        info.setEmail(email);
        info.setBasisrol(basisrol);
        return info;
    }

    private SmartschoolGroupInfoResponseDTO buildGroupResponse(String... classNames) {
        SmartschoolGroupDTO[] groups = Arrays.stream(classNames)
                .map(name -> new SmartschoolGroupDTO(null, name, null, null, null, null))
                .toArray(SmartschoolGroupDTO[]::new);
        return new SmartschoolGroupInfoResponseDTO(groups, new SmartschoolGroupDTO[0]);
    }

    private void stubExistingUser(User user, String smartschoolUsername, String email, int id) {
        when(userRepository.findBySmartschoolUsername(smartschoolUsername)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot(email, id)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void performCallback() throws Exception {
        mockMvc.perform(get("/oauth/smartschool/callback")
                        .param("code", "validcode")
                        .param("state", "matchingstate")
                        .cookie(new jakarta.servlet.http.Cookie("oauth_state", "matchingstate")))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/oauth-callback?token=")));
    }

    private User captureLastSavedUser(String email) {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues().stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("User with email " + email + " was not saved"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_existingUser_classExistsOnPlatform_userAddedToClass() throws Exception {
        SmartschoolUserInfo info = buildUserInfo("jan.peeters", "jan@school.be", "leerling");
        when(mockRestTemplate.getForObject(contains("fulluserinfo"), eq(SmartschoolUserInfo.class))).thenReturn(info);
        when(mockRestTemplate.getForObject(contains("groupinfo"), eq(SmartschoolGroupInfoResponseDTO.class))).thenReturn(buildGroupResponse("2B"));

        User existing = buildUser(5, "jan@school.be", Role.LEERLING);
        existing.setSmartschoolUsername("jan.peeters");
        stubExistingUser(existing, "jan.peeters", "jan@school.be", 5);

        SchoolClass class2B = new SchoolClass("2B", defaultSchool, new ArrayList<>());
        when(classService.getClassByNameAndSchoolId("2B", 1L)).thenReturn(class2B);

        performCallback();

        ArgumentCaptor<SchoolClass> captor = ArgumentCaptor.forClass(SchoolClass.class);
        verify(classService).addClass(captor.capture());
        assertEquals("2B", captor.getValue().getClassName());
        assertTrue(captor.getValue().getUsers().contains(existing));
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_classDoesNotExistOnPlatform_newSchoolClassCreated() throws Exception {
        SmartschoolUserInfo info = buildUserInfo("jan.peeters", "jan@school.be", "leerling");
        when(mockRestTemplate.getForObject(contains("fulluserinfo"), eq(SmartschoolUserInfo.class))).thenReturn(info);
        when(mockRestTemplate.getForObject(contains("groupinfo"), eq(SmartschoolGroupInfoResponseDTO.class))).thenReturn(buildGroupResponse("3C"));

        User existing = buildUser(5, "jan@school.be", Role.LEERLING);
        existing.setSmartschoolUsername("jan.peeters");
        stubExistingUser(existing, "jan.peeters", "jan@school.be", 5);

        when(classService.getClassByNameAndSchoolId("3C", 1L)).thenReturn(null);

        performCallback();

        ArgumentCaptor<SchoolClass> captor = ArgumentCaptor.forClass(SchoolClass.class);
        verify(classService).addClass(captor.capture());
        assertEquals("3C", captor.getValue().getClassName());
        assertTrue(captor.getValue().getUsers().contains(existing));
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_nullGroupInfo_addClassNeverCalled() throws Exception {
        SmartschoolUserInfo info = buildUserInfo("jan.peeters", "jan@school.be", "leerling");
        when(mockRestTemplate.getForObject(contains("fulluserinfo"), eq(SmartschoolUserInfo.class))).thenReturn(info);
        when(mockRestTemplate.getForObject(contains("groupinfo"), eq(SmartschoolGroupInfoResponseDTO.class))).thenReturn(null);

        User existing = buildUser(5, "jan@school.be", Role.LEERLING);
        existing.setSmartschoolUsername("jan.peeters");
        stubExistingUser(existing, "jan.peeters", "jan@school.be", 5);

        performCallback();

        verify(classService, never()).addClass(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_userAlreadyInClass_notAddedAgain() throws Exception {
        SmartschoolUserInfo info = buildUserInfo("jan.peeters", "jan@school.be", "leerling");
        when(mockRestTemplate.getForObject(contains("fulluserinfo"), eq(SmartschoolUserInfo.class))).thenReturn(info);
        when(mockRestTemplate.getForObject(contains("groupinfo"), eq(SmartschoolGroupInfoResponseDTO.class))).thenReturn(buildGroupResponse("2B"));

        User existing = buildUser(5, "jan@school.be", Role.LEERLING);
        existing.setSmartschoolUsername("jan.peeters");
        stubExistingUser(existing, "jan.peeters", "jan@school.be", 5);

        SchoolClass class2B = new SchoolClass("2B", defaultSchool, new ArrayList<>(List.of(existing)));
        when(classService.getClassByNameAndSchoolId("2B", 1L)).thenReturn(class2B);

        performCallback();

        ArgumentCaptor<SchoolClass> captor = ArgumentCaptor.forClass(SchoolClass.class);
        verify(classService).addClass(captor.capture());
        assertEquals(1, captor.getValue().getUsers().stream().filter(u -> u.equals(existing)).count());
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_userInMultipleGroups_addClassCalledForEachGroup() throws Exception {
        SmartschoolUserInfo info = buildUserInfo("jan.peeters", "jan@school.be", "leerling");
        when(mockRestTemplate.getForObject(contains("fulluserinfo"), eq(SmartschoolUserInfo.class))).thenReturn(info);
        when(mockRestTemplate.getForObject(contains("groupinfo"), eq(SmartschoolGroupInfoResponseDTO.class))).thenReturn(buildGroupResponse("2A", "2B"));

        User existing = buildUser(5, "jan@school.be", Role.LEERLING);
        existing.setSmartschoolUsername("jan.peeters");
        stubExistingUser(existing, "jan.peeters", "jan@school.be", 5);

        when(classService.getClassByNameAndSchoolId(anyString(), eq(1L))).thenReturn(null);

        performCallback();

        verify(classService, times(2)).addClass(any(SchoolClass.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_noMatchingOneRosterSchool_classesNotSynced() throws Exception {
        // Stub OneRoster to return a school that doesn't exist in our DB
        stubOneRoster("Test", "User", "Non Existent School");

        SmartschoolUserInfo info = buildUserInfo("jan.peeters", "jan@school.be", "leerling");
        when(mockRestTemplate.getForObject(contains("fulluserinfo"), eq(SmartschoolUserInfo.class))).thenReturn(info);
        when(mockRestTemplate.getForObject(contains("groupinfo"), eq(SmartschoolGroupInfoResponseDTO.class))).thenReturn(buildGroupResponse("2B"));

        when(userRepository.findBySmartschoolUsername("jan.peeters")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("jan@school.be")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        performCallback();

        // syncUserClasses should return early because user.getSchool() is null
        verify(classService, never()).addClass(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_userChangedClass_removedFromOldClass_addedToNewClass() throws Exception {
        SmartschoolUserInfo info = buildUserInfo("jan.peeters", "jan@school.be", "leerling");
        when(mockRestTemplate.getForObject(contains("fulluserinfo"), eq(SmartschoolUserInfo.class))).thenReturn(info);
        when(mockRestTemplate.getForObject(contains("groupinfo"), eq(SmartschoolGroupInfoResponseDTO.class))).thenReturn(buildGroupResponse("2B"));

        User existing = buildUser(5, "jan@school.be", Role.LEERLING);
        existing.setSmartschoolUsername("jan.peeters");
        stubExistingUser(existing, "jan.peeters", "jan@school.be", 5);

        SchoolClass oldClass2A = new SchoolClass("2A", defaultSchool, new ArrayList<>(List.of(existing)));
        SchoolClass newClass2B = new SchoolClass("2B", defaultSchool, new ArrayList<>());
        when(classService.getClassesByUser(existing)).thenReturn(List.of(oldClass2A));
        when(classService.getClassByNameAndSchoolId("2B", 1L)).thenReturn(newClass2B);

        performCallback();

        ArgumentCaptor<SchoolClass> captor = ArgumentCaptor.forClass(SchoolClass.class);
        verify(classService, atLeastOnce()).addClass(captor.capture());
        List<SchoolClass> saved = captor.getAllValues();

        SchoolClass saved2A = saved.stream()
                .filter(c -> "2A".equals(c.getClassName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Class 2A was not saved"));
        assertFalse(saved2A.getUsers().contains(existing), "User should be removed from class 2A");

        SchoolClass saved2B = saved.stream()
                .filter(c -> "2B".equals(c.getClassName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Class 2B was not saved"));
        assertTrue(saved2B.getUsers().contains(existing), "User should be added to class 2B");
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_userChangedClass_favoriteBooksPreserved() throws Exception {
        SmartschoolUserInfo info = buildUserInfo("jan.peeters", "jan@school.be", "leerling");
        when(mockRestTemplate.getForObject(contains("fulluserinfo"), eq(SmartschoolUserInfo.class))).thenReturn(info);
        when(mockRestTemplate.getForObject(contains("groupinfo"), eq(SmartschoolGroupInfoResponseDTO.class))).thenReturn(buildGroupResponse("2B"));

        User existing = buildUser(5, "jan@school.be", Role.LEERLING);
        existing.setSmartschoolUsername("jan.peeters");

        Item favoriteBook = new Item();
        favoriteBook.setId(99L);
        favoriteBook.setTitle("The Hobbit");
        existing.getFavoriteBooks().add(favoriteBook);

        stubExistingUser(existing, "jan.peeters", "jan@school.be", 5);

        when(classService.getClassesByUser(existing)).thenReturn(List.of(
                new SchoolClass("2A", defaultSchool, new ArrayList<>(List.of(existing)))));
        when(classService.getClassByNameAndSchoolId("2B", 1L))
                .thenReturn(new SchoolClass("2B", defaultSchool, new ArrayList<>()));

        performCallback();

        User savedUser = captureLastSavedUser("jan@school.be");
        assertTrue(savedUser.getFavoriteBooks().contains(favoriteBook),
                "Favorite books must be preserved after class change");
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_userChangedClass_borrowedCountPreserved() throws Exception {
        SmartschoolUserInfo info = buildUserInfo("jan.peeters", "jan@school.be", "leerling");
        when(mockRestTemplate.getForObject(contains("fulluserinfo"), eq(SmartschoolUserInfo.class))).thenReturn(info);
        when(mockRestTemplate.getForObject(contains("groupinfo"), eq(SmartschoolGroupInfoResponseDTO.class))).thenReturn(buildGroupResponse("2B"));

        User existing = buildUser(5, "jan@school.be", Role.LEERLING);
        existing.setSmartschoolUsername("jan.peeters");
        existing.setItemsBorrowed(3);

        stubExistingUser(existing, "jan.peeters", "jan@school.be", 5);

        when(classService.getClassesByUser(existing)).thenReturn(List.of(
                new SchoolClass("2A", defaultSchool, new ArrayList<>(List.of(existing)))));
        when(classService.getClassByNameAndSchoolId("2B", 1L))
                .thenReturn(new SchoolClass("2B", defaultSchool, new ArrayList<>()));

        performCallback();

        User savedUser = captureLastSavedUser("jan@school.be");
        assertEquals(3, savedUser.getItemsBorrowed(),
                "Borrowed count must be preserved after class change");
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_userChangedClass_personalDataPreserved() throws Exception {
        SmartschoolUserInfo info = buildUserInfo("jan.peeters", "jan@school.be", "leerling");
        when(mockRestTemplate.getForObject(contains("fulluserinfo"), eq(SmartschoolUserInfo.class))).thenReturn(info);
        when(mockRestTemplate.getForObject(contains("groupinfo"), eq(SmartschoolGroupInfoResponseDTO.class))).thenReturn(buildGroupResponse("2B"));

        User existing = buildUser(5, "jan@school.be", Role.LEERLING);
        existing.setSmartschoolUsername("jan.peeters");
        existing.setStudentNumber("S12345");

        stubExistingUser(existing, "jan.peeters", "jan@school.be", 5);

        when(classService.getClassesByUser(existing)).thenReturn(List.of(
                new SchoolClass("2A", defaultSchool, new ArrayList<>(List.of(existing)))));
        when(classService.getClassByNameAndSchoolId("2B", 1L))
                .thenReturn(new SchoolClass("2B", defaultSchool, new ArrayList<>()));

        performCallback();

        User savedUser = captureLastSavedUser("jan@school.be");
        assertEquals("jan@school.be", savedUser.getEmail());
        assertEquals(Role.LEERLING, savedUser.getRole());
        assertEquals("S12345", savedUser.getStudentNumber(),
                "Student number must be preserved after class change");
    }
}