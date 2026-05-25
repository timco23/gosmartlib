package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Sends Smartschool notifications and messages to users via the Smartschool API.
 * The sendNotification call (/Api/V1/sendnotif) only goes to the main account.
 * So we use sendMessage for anything parents should also see (reminders, late notices),
 * and sendNotification for personal notifications (lending confirmation, extension, etc.).
 */
@Service
public class SmartschoolNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmartschoolNotificationService.class);

    private static final String TOKEN_PATH = "/OAuth/index/token";
    private static final String NOTIF_PATH = "/Api/V1/sendnotif";
    private static final String MSG_PATH   = "/Api/V1/sendmsg";

    @Value("${smartschool.platform}")
    private String platform;

    @Value("${smartschool.client-id}")
    private String clientId;

    @Value("${smartschool.client-secret}")
    private String clientSecret;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;

    public SmartschoolNotificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Sends a bell notification (quick popup) to the user ONLY.
     * Co-accounts (parents) do NOT receive this 
     * @param user       recipient
     * @param title      short title
     * @param description one-sentence description
     * @param relativeUrl relative frontend path (e.g. "/dashboard")
     */
    public void sendNotification(User user, String title, String description, String relativeUrl) {
        if (!hasSmartschoolAccount(user)) return;

        String token = resolveAccessToken(user);
        if (token == null) {
            log.warn("Cannot send notification to {} — no valid access token", user.getEmail());
            return;
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://" + platform + NOTIF_PATH)
                .queryParam("access_token", token)
                .queryParam("notifTitle",   encode(title))
                .queryParam("notifDesc",    encode(description))
                .queryParam("notifUrl",     encode(frontendBaseUrl + relativeUrl))
                .toUriString();

        try {
            restTemplate.getForObject(url, String.class);
            log.debug("Notification sent to {}: {}", user.getEmail(), title);
        } catch (Exception e) {
            log.error("Failed to send notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Sends a full Smartschool message to the user's inbox.
     * Smartschool automatically delivers this to ALL co-accounts
     * (parents) of the user as well. This is the correct channel for anything
     * parents should also see (reminders, late notices).
     * @param user   recipient (message also goes to their co-accounts/parents)
     * @param title  message subject
     * @param body   HTML or plain-text body
     */
    public void sendMessage(User user, String title, String body) {
        if (!hasSmartschoolAccount(user)) return;

        String token = resolveAccessToken(user);
        if (token == null) {
            log.warn("Cannot send message to {} — no valid access token", user.getEmail());
            return;
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://" + platform + MSG_PATH)
                .queryParam("access_token", token)
                .queryParam("messageTitle", encode(title))
                .queryParam("messageBody",  encode(body))
                .toUriString();

        try {
            restTemplate.getForObject(url, String.class);
            log.debug("Message sent to {}: {}", user.getEmail(), title);
        } catch (Exception e) {
            log.error("Failed to send message to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    // Sends both a notification (student only) and a message (student + parents)
    public void sendNotificationAndMessage(User user,
                                           String notifTitle, String notifDesc, String relativeUrl,
                                           String msgTitle,   String msgBody) {
        sendNotification(user, notifTitle, notifDesc, relativeUrl);
        sendMessage(user, msgTitle, msgBody);
    }

    // Sends ONLY a notification (no message inbox).
    public void sendPersonalNotification(User user, String title, String description, String relativeUrl) {
        sendNotification(user, title, description, relativeUrl);
    }

    private String resolveAccessToken(User user) {
        if (user.getSmartschoolAccessToken() != null) {
            if (isTokenValid(user.getSmartschoolAccessToken())) {
                return user.getSmartschoolAccessToken();
            }
        }
        if (user.getRefreshToken() == null) return null;
        return refreshAccessToken(user);
    }

    @SuppressWarnings("unchecked")
    private boolean isTokenValid(String token) {
        try {
            String url = "https://" + platform + "/Api/V1/userinfo?access_token=" + encode(token);
            ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private String refreshAccessToken(User user) {
        try {
            String tokenUrl = "https://" + platform + TOKEN_PATH;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type",    "refresh_token");
            form.add("refresh_token", user.getRefreshToken());
            form.add("client_id",     clientId);
            form.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Token refresh failed for {}", user.getEmail());
                return null;
            }

            Map<String, Object> body = response.getBody();
            String newAccessToken  = (String) body.get("access_token");
            String newRefreshToken = (String) body.getOrDefault("refresh_token", user.getRefreshToken());

            user.setSmartschoolAccessToken(newAccessToken);
            user.setRefreshToken(newRefreshToken);
            userRepository.save(user);

            log.debug("Refreshed access token for {}", user.getEmail());
            return newAccessToken;

        } catch (Exception e) {
            log.error("Error refreshing token for {}: {}", user.getEmail(), e.getMessage());
            return null;
        }
    }

    private boolean hasSmartschoolAccount(User user) {
        return user != null && user.getSmartschoolUsername() != null;
    }

    private static String encode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}