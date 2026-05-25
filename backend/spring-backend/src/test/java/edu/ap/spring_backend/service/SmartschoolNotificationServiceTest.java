package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmartschoolNotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SmartschoolNotificationService service;

    private RestTemplate mockRestTemplate;

    @BeforeEach
    void setUp() {
        mockRestTemplate = Mockito.mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", mockRestTemplate);
        ReflectionTestUtils.setField(service, "platform", "test.smartschool.be");
        ReflectionTestUtils.setField(service, "clientId", "test-client-id");
        ReflectionTestUtils.setField(service, "clientSecret", "test-secret");
        ReflectionTestUtils.setField(service, "frontendBaseUrl", "http://localhost:3000");
    }

    @Test
    void sendNotification_noSmartschoolAccount_shouldDoNothing() {
        User user = new User();
        user.setEmail("local@school.be");

        service.sendNotification(user, "Titel", "Beschrijving", "/dashboard");

        verifyNoInteractions(mockRestTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendNotification_withValidToken_shouldCallSendnotif() {
        User user = buildUserWithToken("jan.peeters", "valid-token", null);
        when(mockRestTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("username", "jan.peeters"), HttpStatus.OK));
        when(mockRestTemplate.getForObject(contains("sendnotif"), eq(String.class)))
                .thenReturn("ok");

        service.sendNotification(user, "Herinnering", "Boek inleveren", "/dashboard");

        verify(mockRestTemplate).getForObject(contains("sendnotif"), eq(String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendNotification_expiredToken_shouldRefreshAndRetry() {
        User user = buildUserWithToken("jan.peeters", "expired-token", "valid-refresh-token");

        when(mockRestTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));

        when(mockRestTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(
                        Map.of("access_token", "new-token", "refresh_token", "new-refresh"),
                        HttpStatus.OK));

        when(mockRestTemplate.getForObject(contains("sendnotif"), eq(String.class)))
                .thenReturn("ok");

        service.sendNotification(user, "Herinnering", "Boek inleveren", "/dashboard");

        verify(userRepository).save(user);
        verify(mockRestTemplate).getForObject(contains("sendnotif"), eq(String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendNotification_noTokenAtAll_shouldDoNothing() {
        User user = new User();
        user.setSmartschoolUsername("jan.peeters");

        service.sendNotification(user, "Titel", "Beschrijving", "/dashboard");

        verify(mockRestTemplate, never()).getForObject(contains("sendnotif"), eq(String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendNotification_refreshFails_shouldDoNothing() {
        User user = buildUserWithToken("jan.peeters", "expired-token", "bad-refresh");

        when(mockRestTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        service.sendNotification(user, "Titel", "Beschrijving", "/dashboard");

        verify(mockRestTemplate, never()).getForObject(contains("sendnotif"), eq(String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendMessage_withValidToken_shouldCallSendmsg() {
        User user = buildUserWithToken("jan.peeters", "valid-token", null);

        when(mockRestTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("username", "jan.peeters"), HttpStatus.OK));
        when(mockRestTemplate.getForObject(contains("sendmsg"), eq(String.class)))
                .thenReturn("ok");

        service.sendMessage(user, "Boek uitgeleend", "<p>Veel leesplezier!</p>");

        verify(mockRestTemplate).getForObject(contains("sendmsg"), eq(String.class));
    }

    @Test
    void sendMessage_noSmartschoolAccount_shouldDoNothing() {
        User user = new User();
        user.setEmail("local@school.be");

        service.sendMessage(user, "Titel", "Body");

        verifyNoInteractions(mockRestTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendNotificationAndMessage_shouldCallBothEndpoints() {
        User user = buildUserWithToken("jan.peeters", "valid-token", null);

        when(mockRestTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("username", "jan.peeters"), HttpStatus.OK));
        when(mockRestTemplate.getForObject(anyString(), eq(String.class))).thenReturn("ok");

        service.sendNotificationAndMessage(
                user,
                "Notif titel", "Notif desc", "/dashboard",
                "Msg titel", "Msg body");

        verify(mockRestTemplate).getForObject(contains("sendnotif"), eq(String.class));
        verify(mockRestTemplate).getForObject(contains("sendmsg"), eq(String.class));
    }


    private User buildUserWithToken(String username, String accessToken, String refreshToken) {
        User user = new User();
        user.setSmartschoolUsername(username);
        user.setEmail(username + "@school.be");
        user.setSmartschoolAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        return user;
    }
}