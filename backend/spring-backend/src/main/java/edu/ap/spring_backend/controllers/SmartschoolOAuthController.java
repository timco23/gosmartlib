package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.SmartschoolGroupDTO;
import edu.ap.spring_backend.dto.SmartschoolGroupInfoResponseDTO;
import edu.ap.spring_backend.dto.SmartschoolUserInfo;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.SchoolClass;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.security.JwtService;
import edu.ap.spring_backend.service.ClassService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.io.IOException;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.jpa.School;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.Duration;

/**
 * Handles the Smartschool OAuth2 login flow.
 * Endpoints:
 * - GET /oauth/smartschool/login: creates a state value, stores it in an HttpOnly cookie,
 *   and redirects the user to Smartschool's authorization page.
 * - GET /oauth/smartschool/callback validates the state, exchanges the code for an access token,
 *   fetches user info, finds/creates a local user, generates a JWT, and redirects to the frontend.
 */
@RestController
public class SmartschoolOAuthController {

    private static final String HTTPS = "https://";
    private static final String ACCESS_TOKEN_PARAM = "access_token";
    @Value("${smartschool.oauth.authorize-path:/OAuth}")
    private String authorizePath;

    @Value("${smartschool.oauth.token-path:/OAuth/index/token}")
    private String tokenPath;

    @Value("${smartschool.api.fulluserinfo-path:/Api/V1/fulluserinfo}")
    private String fullUserInfoPath;

    @Value("${smartschool.api.groupinfo-path:/Api/V1/groupinfo}")
    private String groupInfoPath;

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final SecureRandom secureRandom = new SecureRandom();
    private final JwtService jwtService;
    private final ClassService classService;
    

    @Value("${smartschool.client-id}")
    private String clientId;

    @Value("${smartschool.client-secret}")
    private String clientSecret;

    @Value("${smartschool.platform}")
    private String platform;

    @Value("${smartschool.redirect-uri}")
    private String redirectUri;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("${oauth.default-school-id:0}")
    private Integer defaultSchoolId;

     /**
     * Creates the controller and initializes {@link JwtService} using application properties.
     * @param userRepository repository for loading/saving users
     * @param jwtSecret JWT signing secret
     * @param expiresMinutes JWT lifetime in minutes
     * @param schoolRepository repository for loading schools
     */
    public SmartschoolOAuthController(
            UserRepository userRepository,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expires-minutes}") long expiresMinutes,
            SchoolRepository schoolRepository,
            ClassService classService,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.schoolRepository = schoolRepository;
        this.classService = classService;
    }

    private School resolveDefaultSchool() {
        if (defaultSchoolId == null || defaultSchoolId <= 0) return null;
        return schoolRepository.findById(defaultSchoolId.longValue()).orElse(null);
    }

    @GetMapping("/oauth/smartschool/login")
    public void login(HttpServletRequest request, HttpServletResponse response) {
        String state = generateState();

        boolean https = isHttpsRequest(request);

        ResponseCookie stateCookie = ResponseCookie.from("oauth_state", state)
                .httpOnly(true)
                .secure(https)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, stateCookie.toString());

        String authorizeUrl = platformUrl(authorizePath);
        String effectiveRedirectUri = getEffectiveRedirectUri(request);

        String redirect = authorizeUrl
                + "?response_type=code"
                + "&client_id=" + url(clientId)
                + "&redirect_uri=" + url(effectiveRedirectUri)
                + "&scope=" + url("fulluserinfo groupinfo")
                + "&state=" + url(state);

        response.setStatus(302);
        response.setHeader("Location", redirect);
    }

     @GetMapping("/oauth/smartschool/callback")
    public void callback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String state = request.getParameter("state");

        if (isMissingCodeOrState(code, state, response)) return;
        if (isStateMismatch(request, state, response)) return;

        String accessToken = exchangeAccessToken(code, request, response);
        if (accessToken == null) return;

        SmartschoolUserInfo info = fetchUserInfo(accessToken, response);
        if (info == null) return;

        SmartschoolGroupInfoResponseDTO groupInfo = fetchGroupInfo(accessToken);

        School defaultSchool = resolveDefaultSchool();
        User user = upsertUser(info, defaultSchool);

        String jwt = jwtService.generateToken(user);
        String refreshToken = generateRefreshToken();

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        syncUserClasses(user, defaultSchool, groupInfo);

        redirectToFrontend(response, jwt, refreshToken);
    }

     private boolean isMissingCodeOrState(String code, String state, HttpServletResponse response) throws IOException {
        if (code == null || state == null) {
            redirectToLogin(response, "error");
            return true;
        }
        return false;
    }

     private boolean isStateMismatch(HttpServletRequest request, String state, HttpServletResponse response) throws IOException {
        String cookieState = readCookie(request, "oauth_state");
        if (cookieState == null || !cookieState.equals(state)) {
            redirectToLogin(response, "state_mismatch");
            return true;
        }
        return false;
    }

     private String exchangeAccessToken(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String tokenUrl = platformUrl(tokenPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", getEffectiveRedirectUri(request));
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(form, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> tokenResponse =
                restTemplate.postForEntity(tokenUrl, tokenRequest, (java.lang.Class<Map<String, Object>>) (java.lang.Class<?>) Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            redirectToLogin(response, "token_error");
            return null;
        }

         Map<String, Object> tokenBody = tokenResponse.getBody();
        if (tokenBody == null) {
            redirectToLogin(response, "token_error");
            return null;
        }

        Object accessTokenObj = tokenBody.get(ACCESS_TOKEN_PARAM);

        if (accessTokenObj == null) {
            redirectToLogin(response, "token_missing");
            return null;
        }

        return String.valueOf(accessTokenObj);
    }

    private SmartschoolUserInfo fetchUserInfo(String accessToken, HttpServletResponse response) throws IOException {
        String userInfoUrl = platformUrl(fullUserInfoPath) + "?" + ACCESS_TOKEN_PARAM + "=" + url(accessToken);
        SmartschoolUserInfo info = restTemplate.getForObject(userInfoUrl, SmartschoolUserInfo.class);

        if (info == null || info.getUsername() == null || info.getUsername().isBlank()) {
            redirectToLogin(response, "userinfo_error");
            return null;
        }
        return info;
    }

    private SmartschoolGroupInfoResponseDTO fetchGroupInfo(String accessToken) {
        String groupInfoUrl = platformUrl(groupInfoPath) + "?" + ACCESS_TOKEN_PARAM + "=" + url(accessToken);
        return restTemplate.getForObject(groupInfoUrl, SmartschoolGroupInfoResponseDTO.class);
    }

    private User upsertUser(SmartschoolUserInfo info, School defaultSchool) {
        User user = userRepository.findBySmartschoolUsername(info.getUsername()).orElse(null);

        String incomingEmail = normalizeEmail(info.getEmail());
        boolean hasIncomingEmail = incomingEmail != null && !incomingEmail.isBlank();

        if (user == null) {
            return createNewUser(info, defaultSchool, incomingEmail, hasIncomingEmail);
        }

        updateExistingUser(user, info, defaultSchool, incomingEmail, hasIncomingEmail);
        return user;
    }

    private User createNewUser(SmartschoolUserInfo info, School defaultSchool, String incomingEmail, boolean hasIncomingEmail) {
        User user = new User();
        user.setSmartschoolUsername(info.getUsername());
        user.setFirstName(info.getName());
        user.setName(info.getSurname());
        user.setRole(mapSmartschoolRole(info.getBasisrol()));
        user.setPassword("{noop}oauth");

        assignDefaultSchoolIfPresent(user, defaultSchool);
        assignEmailForNewUser(user, info.getUsername(), incomingEmail, hasIncomingEmail);

        return user;
    }

    private void updateExistingUser(User user, SmartschoolUserInfo info, School defaultSchool, String incomingEmail, boolean hasIncomingEmail) {
        user.setFirstName(info.getName());
        user.setName(info.getSurname());

        maybeUpdateRole(user, info);
        maybeUpdateEmail(user, incomingEmail, hasIncomingEmail);
        assignDefaultSchoolIfMissing(user, defaultSchool);
    }

    private void maybeUpdateRole(User user, SmartschoolUserInfo info) {
        Role currentRole = user.getRole();
        if (currentRole == null || currentRole == Role.LEERLING || currentRole == Role.LEERKRACHT) {
            user.setRole(mapSmartschoolRole(info.getBasisrol()));
        }
    }

    private void maybeUpdateEmail(User user, String incomingEmail, boolean hasIncomingEmail) {
        if (hasIncomingEmail && !userRepository.existsByEmailAndIdNot(incomingEmail, user.getId())) {
            user.setEmail(incomingEmail);
        }
    }

    private void assignDefaultSchoolIfPresent(User user, School defaultSchool) {
        if (defaultSchool != null) {
            user.setSchool(defaultSchool);
        }
    }

    private void assignDefaultSchoolIfMissing(User user, School defaultSchool) {
        if (user.getSchool() == null && defaultSchool != null) {
            user.setSchool(defaultSchool);
        }
    }

    private void assignEmailForNewUser(User user, String username, String incomingEmail, boolean hasIncomingEmail) {
        if (hasIncomingEmail && !userRepository.existsByEmail(incomingEmail)) {
            user.setEmail(incomingEmail);
        } else {
            user.setEmail(username + "@smartschool.local");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private void syncUserClasses(User user, School defaultSchool, SmartschoolGroupInfoResponseDTO groupInfo) {
        if (groupInfo == null || groupInfo.groups() == null || defaultSchool == null) return;

        // Names of classes currently returned by Smartschool
        Set<String> smartschoolClassNames = Arrays.stream(groupInfo.groups())
                .map(SmartschoolGroupDTO::name)
                .collect(java.util.stream.Collectors.toSet());

        // Remove the student from classes that are no longer listed in Smartschool
        List<SchoolClass> currentClasses = classService.getClassesByUser(user);
        for (SchoolClass currentClass : currentClasses) {
            if (!smartschoolClassNames.contains(currentClass.getClassName())) {
                currentClass.getUsers().remove(user);
                classService.addClass(currentClass);
            }
        }

        // Add the student to the classes returned by Smartschool
        for (SmartschoolGroupDTO group : groupInfo.groups()) {
            SchoolClass schoolClass = classService.getClassByNameAndSchoolId(group.name(), defaultSchool.getId());

            if (schoolClass == null) {
                List<User> users = new ArrayList<>();
                users.add(user);
                schoolClass = new SchoolClass(group.name(), defaultSchool, users);
            } else if (!schoolClass.getUsers().contains(user)) {
                schoolClass.getUsers().add(user);
            }
            classService.addClass(schoolClass);
        }
    }

    private void redirectToLogin(HttpServletResponse response, String reason) throws IOException {
        response.sendRedirect(frontendBaseUrl + "/login?oauth=" + reason);
    }

    private void redirectToFrontend(HttpServletResponse response, String jwt, String refreshToken) throws IOException {
        String redirectUrl = frontendBaseUrl
                + "/oauth-callback?token=" + url(jwt)
                + "&refreshToken=" + url(refreshToken);
        response.sendRedirect(redirectUrl);
    }

    private String platformUrl(String path) {
        return HTTPS + platform + path;
    }

     private boolean isHttpsRequest(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return request.isSecure() || "https".equalsIgnoreCase(forwardedProto);
    }

    private String getEffectiveRedirectUri(HttpServletRequest request) {
        if (!isHttpsRequest(request)) return redirectUri;
        return UriComponentsBuilder.fromUriString(redirectUri)
                .scheme("https")
                .build(true)
                .toUriString();
    }

    /**
     * Maps Smartschool role strings to local Role enum.
     * Smartschool returns "leerkracht" or "leerling".
     * ADMIN and BIBLIOTHEEKBEHEERDER must be manually assigned (they don't come from Smartschool).
     * @param baseRole the role string from Smartschool user info
     * @return the corresponding Role enum value (defaulting to LEERLING if unrecognized
     * */
    private Role mapSmartschoolRole(String baseRole) {
        if (baseRole == null) return Role.LEERLING;
        String r = baseRole.trim().toLowerCase();

        if (r.contains("leerkracht") || r.contains("leraar") || r.contains("personeel")) {
            return Role.LEERKRACHT;
        }
        if (r.contains("leerling")) {
            return Role.LEERLING;
        }
        return Role.LEERLING;
    }

    /**
     * Generates a URL-safe random OAuth state value.
     * @return random state string
     */
    private String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Generates a long-lived refresh token (UUID).
     * This is stored in the database and used to get a new JWT when it expires.
     * @return refresh token string
     */
    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * URL-encodes a string for use in query parameters.
     * @param s input value
     * @return encoded value
     */
    private static String url(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /**
     * Reads a cookie value from the request.
     * @param request incoming request
     * @param name cookie name
     * @return cookie value or null if not found
     */
    private static String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}