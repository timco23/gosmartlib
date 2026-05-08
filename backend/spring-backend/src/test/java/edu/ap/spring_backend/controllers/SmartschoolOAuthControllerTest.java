package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.SmartschoolUserInfo;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import java.lang.reflect.Field;
import java.util.Map;
import edu.ap.spring_backend.service.ClassService;
import java.util.Optional;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

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
        "app.jwt.expires-minutes=60",
        "oauth.default-school-id=0"
})
class SmartschoolOAuthControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchoolRepository schoolRepository;

    @MockBean
    private ClassService classService;

    @Autowired
    private SmartschoolOAuthController controller;

    private RestTemplate mockRestTemplate;

    @BeforeEach
    void injectMockRestTemplate() throws Exception {
        mockRestTemplate = Mockito.mock(RestTemplate.class);
        Field field = SmartschoolOAuthController.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(controller, mockRestTemplate);

        Mockito.lenient().when(jwtService.generateToken(any())).thenReturn("fake-jwt");
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

    @Test
    void login_shouldRedirectToSmartschool() throws Exception {
        mockMvc.perform(get("/oauth/smartschool/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("test.smartschool.be/OAuth")))
                .andExpect(header().string("Location", containsString("client_id=test-client-id")))
                .andExpect(header().string("Set-Cookie", containsString("oauth_state=")));
    }

    @Test
    void login_shouldSetHttpOnlyStateCookie() throws Exception {
        mockMvc.perform(get("/oauth/smartschool/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")));
    }

    // /oauth/smartschool/callback

    @Test
    void callback_missingCodeOrState_shouldRedirectToError() throws Exception {
        mockMvc.perform(get("/oauth/smartschool/callback"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login?oauth=error")));
    }

    @Test
    void callback_stateMismatch_shouldRedirectToStateMismatch() throws Exception {
        mockMvc.perform(get("/oauth/smartschool/callback")
                        .param("code", "somecode")
                        .param("state", "wrongstate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login?oauth=state_mismatch")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_tokenEndpointFails_shouldRedirectToTokenError() throws Exception {
        when(mockRestTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get("/oauth/smartschool/callback")
                        .param("code", "validcode")
                        .param("state", "matchingstate")
                        .cookie(new jakarta.servlet.http.Cookie("oauth_state", "matchingstate")))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login?oauth=token_error")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_missingAccessToken_shouldRedirectToTokenMissing() throws Exception {
        when(mockRestTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(Map.of(), HttpStatus.OK));

        mockMvc.perform(get("/oauth/smartschool/callback")
                        .param("code", "validcode")
                        .param("state", "matchingstate")
                        .cookie(new jakarta.servlet.http.Cookie("oauth_state", "matchingstate")))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login?oauth=token_missing")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_userinfoEmpty_shouldRedirectToUserinfoError() throws Exception {
        when(mockRestTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(Map.of("access_token", "fake-token"), HttpStatus.OK));
        when(mockRestTemplate.getForObject(anyString(), eq(SmartschoolUserInfo.class)))
                .thenReturn(null);

        mockMvc.perform(get("/oauth/smartschool/callback")
                        .param("code", "validcode")
                        .param("state", "matchingstate")
                        .cookie(new jakarta.servlet.http.Cookie("oauth_state", "matchingstate")))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login?oauth=userinfo_error")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_newUser_shouldCreateAndRedirectToFrontend() throws Exception {
        when(mockRestTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(Map.of("access_token", "fake-token"), HttpStatus.OK));

        SmartschoolUserInfo info = new SmartschoolUserInfo();
        info.setUsername("jan.peeters");
        info.setName("Jan");
        info.setSurname("Peeters");
        info.setEmail("jan.peeters@school.be");
        info.setBasisrol("leerling");
        when(mockRestTemplate.getForObject(anyString(), eq(SmartschoolUserInfo.class))).thenReturn(info);

        when(userRepository.findBySmartschoolUsername("jan.peeters")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("jan.peeters@school.be")).thenReturn(false);

        User savedUser = buildUser(1, "jan.peeters@school.be", Role.LEERLING);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(get("/oauth/smartschool/callback")
                        .param("code", "validcode")
                        .param("state", "matchingstate")
                        .cookie(new jakarta.servlet.http.Cookie("oauth_state", "matchingstate")))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/oauth-callback?token=")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_existingUser_shouldUpdateAndRedirectToFrontend() throws Exception {
        when(mockRestTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(Map.of("access_token", "fake-token"), HttpStatus.OK));

        SmartschoolUserInfo info = new SmartschoolUserInfo();
        info.setUsername("bestaande.user");
        info.setName("Bestaande");
        info.setSurname("User");
        info.setEmail("bestaande@school.be");
        info.setBasisrol("leerkracht");
        when(mockRestTemplate.getForObject(anyString(), eq(SmartschoolUserInfo.class))).thenReturn(info);

        User existing = buildUser(2, "bestaande@school.be", Role.LEERKRACHT);
        existing.setSmartschoolUsername("bestaande.user");
        when(userRepository.findBySmartschoolUsername("bestaande.user")).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("bestaande@school.be", 2)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existing);

        mockMvc.perform(get("/oauth/smartschool/callback")
                        .param("code", "validcode")
                        .param("state", "matchingstate")
                        .cookie(new jakarta.servlet.http.Cookie("oauth_state", "matchingstate")))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/oauth-callback?token=")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_newUser_emailInUse_shouldUseFallbackEmail() throws Exception {
        when(mockRestTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(Map.of("access_token", "fake-token"), HttpStatus.OK));

        SmartschoolUserInfo info = new SmartschoolUserInfo();
        info.setUsername("jan.peeters");
        info.setName("Jan");
        info.setSurname("Peeters");
        info.setEmail("jan.peeters@school.be");
        info.setBasisrol("leerling");
        when(mockRestTemplate.getForObject(anyString(), eq(SmartschoolUserInfo.class))).thenReturn(info);

        when(userRepository.findBySmartschoolUsername("jan.peeters")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("jan.peeters@school.be")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(get("/oauth/smartschool/callback")
                        .param("code", "validcode")
                        .param("state", "matchingstate")
                        .cookie(new jakarta.servlet.http.Cookie("oauth_state", "matchingstate")))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("jan.peeters@smartschool.local", captor.getValue().getEmail());
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_existingUser_adminRole_shouldNotBeOverwritten() throws Exception {
        when(mockRestTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(Map.of("access_token", "fake-token"), HttpStatus.OK));

        SmartschoolUserInfo info = new SmartschoolUserInfo();
        info.setUsername("admin.user");
        info.setName("Admin");
        info.setSurname("User");
        info.setEmail("admin@school.be");
        info.setBasisrol("leerling");
        when(mockRestTemplate.getForObject(anyString(), eq(SmartschoolUserInfo.class))).thenReturn(info);

        User existing = buildUser(10, "admin@school.be", Role.ADMIN);
        existing.setSmartschoolUsername("admin.user");
        when(userRepository.findBySmartschoolUsername("admin.user")).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("admin@school.be", 10)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(get("/oauth/smartschool/callback")
                        .param("code", "validcode")
                        .param("state", "matchingstate")
                        .cookie(new jakarta.servlet.http.Cookie("oauth_state", "matchingstate")))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
    }

    @Test
    @SuppressWarnings("unchecked")
    void callback_existingUser_emailInUse_shouldNotUpdateEmail() throws Exception {
        when(mockRestTemplate.postForEntity(anyString(), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(Map.of("access_token", "fake-token"), HttpStatus.OK));

        SmartschoolUserInfo info = new SmartschoolUserInfo();
        info.setUsername("existing.user");
        info.setName("Existing");
        info.setSurname("User");
        info.setEmail("new@school.be");
        info.setBasisrol("leerkracht");
        when(mockRestTemplate.getForObject(anyString(), eq(SmartschoolUserInfo.class))).thenReturn(info);

        User existing = buildUser(11, "old@school.be", Role.LEERKRACHT);
        existing.setSmartschoolUsername("existing.user");
        when(userRepository.findBySmartschoolUsername("existing.user")).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("new@school.be", 11)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(get("/oauth/smartschool/callback")
                        .param("code", "validcode")
                        .param("state", "matchingstate")
                        .cookie(new jakarta.servlet.http.Cookie("oauth_state", "matchingstate")))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("old@school.be", captor.getValue().getEmail());
    }
}