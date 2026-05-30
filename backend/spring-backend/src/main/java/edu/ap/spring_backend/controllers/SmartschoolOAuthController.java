package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.OneRosterUserDTO;
import edu.ap.spring_backend.dto.OneRosterEnrollmentDTO;
import edu.ap.spring_backend.dto.OneRosterEnrollmentsResponseDTO;
import edu.ap.spring_backend.dto.OneRosterSchoolDTO;
import edu.ap.spring_backend.dto.SmartschoolGroupDTO;
import edu.ap.spring_backend.dto.SmartschoolGroupInfoResponseDTO;
import edu.ap.spring_backend.dto.SmartschoolUserInfo;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.SchoolClass;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.security.JwtService;
import edu.ap.spring_backend.service.ClassService;
import edu.ap.spring_backend.service.OneRosterOAuthService;
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
import java.util.stream.Collectors;

/**
 * Handles the Smartschool OAuth2 login flow.
 * Endpoints:
 * - GET /oauth/smartschool/login: creates a state value, stores it in an HttpOnly cookie,
 *   and redirects the user to Smartschool's authorization page.
 * - GET /oauth/smartschool/callback validates the state, exchanges the code for an access token,
 *   fetches fulluserinfo, finds/creates a local user, generates a JWT, and redirects to the frontend.
 *  The access_token returned by Smartschool is now persisted on the User entity
 *    (user.smartschoolAccessToken). This is needed so SmartschoolNotificationService
 *    can send notifications on behalf of users without requiring them to be online.
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
    private final OneRosterOAuthService oneRosterOAuthService;

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

    public SmartschoolOAuthController(
            UserRepository userRepository,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expires-minutes}") long expiresMinutes,
            SchoolRepository schoolRepository,
            ClassService classService,
            JwtService jwtService,
            OneRosterOAuthService oneRosterOAuthService
    ) {
        this.userRepository  = userRepository;
        this.jwtService      = jwtService;
        this.schoolRepository = schoolRepository;
        this.classService    = classService;
        this.oneRosterOAuthService = oneRosterOAuthService;
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
                + "&scope=" + url("fulluserinfo groupinfo sendmessage sendnotif")
                + "&state=" + url(state);

        response.setStatus(302);
        response.setHeader("Location", redirect);
    }

    @GetMapping("/oauth/smartschool/callback")
    public void callback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code  = request.getParameter("code");
        String state = request.getParameter("state");

        if (isMissingCodeOrState(code, state, response)) return;
        if (isStateMismatch(request, state, response)) return;

        // Exchange code for tokens — we now capture BOTH access_token and refresh_token
        TokenPair tokens = exchangeTokens(code, request, response);
        if (tokens == null) return;

        SmartschoolUserInfo info = fetchUserInfo(tokens.accessToken, response);
        if (info == null) return;

        SmartschoolGroupInfoResponseDTO groupInfo = fetchGroupInfo(tokens.accessToken);

        School defaultSchool = resolveDefaultSchool();
        User user = upsertUser(info, defaultSchool);

        // Persist Smartschool tokens so SmartschoolNotificationService can use them
        user.setSmartschoolAccessToken(tokens.accessToken);
        if (tokens.refreshToken != null) {
            user.setRefreshToken(tokens.refreshToken);
        }

        String jwt = jwtService.generateToken(user);

        // If no refresh token came from Smartschool, keep our own UUID-based one
        if (tokens.refreshToken == null && user.getRefreshToken() == null) {
            user.setRefreshToken(generateRefreshToken());
        }

        //If user doesn't have their OneRoster id linked, link it
        if(user.getOnerosterId() == null) {
            linkOneRosterId(user, groupInfo, info);
        }

        userRepository.save(user);
        syncUserClasses(user, defaultSchool, groupInfo);
        redirectToFrontend(response, jwt, user.getRefreshToken());
    }

    private record TokenPair(String accessToken, String refreshToken) {}

    @SuppressWarnings("unchecked")
    private TokenPair exchangeTokens(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String tokenUrl = platformUrl(tokenPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type",    "authorization_code");
        form.add("code",          code);
        form.add("redirect_uri",  getEffectiveRedirectUri(request));
        form.add("client_id",     clientId);
        form.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(form, headers);
        ResponseEntity<Map<String, Object>> tokenResponse =
                restTemplate.postForEntity(tokenUrl, tokenRequest,
                        (Class<Map<String, Object>>) (Class<?>) Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            redirectToLogin(response, "token_error");
            return null;
        }

        Map<String, Object> body = tokenResponse.getBody();
        Object accessTokenObj  = body.get(ACCESS_TOKEN_PARAM);
        Object refreshTokenObj = body.get("refresh_token");

        if (accessTokenObj == null) {
            redirectToLogin(response, "token_missing");
            return null;
        }

        return new TokenPair(
                String.valueOf(accessTokenObj),
                refreshTokenObj != null ? String.valueOf(refreshTokenObj) : null
        );
    }

    private boolean isMissingCodeOrState(String code, String state, HttpServletResponse response) throws IOException {
        if (code == null || state == null) { redirectToLogin(response, "error"); return true; }
        return false;
    }

    private boolean isStateMismatch(HttpServletRequest request, String state, HttpServletResponse response) throws IOException {
        String cookieState = readCookie(request, "oauth_state");
        if (cookieState == null || !cookieState.equals(state)) { redirectToLogin(response, "state_mismatch"); return true; }
        return false;
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
        if (user == null) return createNewUser(info, defaultSchool, incomingEmail, hasIncomingEmail);
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
        Role current = user.getRole();
        if (current == null || current == Role.LEERLING || current == Role.LEERKRACHT)
            user.setRole(mapSmartschoolRole(info.getBasisrol()));
    }

    private void maybeUpdateEmail(User user, String incomingEmail, boolean hasIncomingEmail) {
        if (hasIncomingEmail && !userRepository.existsByEmailAndIdNot(incomingEmail, user.getId()))
            user.setEmail(incomingEmail);
    }

    private void assignDefaultSchoolIfPresent(User user, School school) { if (school != null) user.setSchool(school); }
    private void assignDefaultSchoolIfMissing(User user, School school) { if (user.getSchool() == null && school != null) user.setSchool(school); }

    private void assignEmailForNewUser(User user, String username, String incomingEmail, boolean hasIncomingEmail) {
        if (hasIncomingEmail && !userRepository.existsByEmail(incomingEmail)) user.setEmail(incomingEmail);
        else user.setEmail(username + "@smartschool.local");
    }

    private String normalizeEmail(String email) { return email == null ? null : email.trim().toLowerCase(); }

    private void syncUserClasses(User user, School defaultSchool, SmartschoolGroupInfoResponseDTO groupInfo) {
        if (groupInfo == null || groupInfo.groups() == null || defaultSchool == null) return;
        Set<String> smartschoolClassNames = Arrays.stream(groupInfo.groups())
                .map(SmartschoolGroupDTO::name)
                .collect(java.util.stream.Collectors.toSet());
        List<SchoolClass> currentClasses = classService.getClassesByUser(user);
        for (SchoolClass c : currentClasses) {
            if (!smartschoolClassNames.contains(c.getClassName())) {
                c.getUsers().remove(user); classService.addClass(c);
            }
        }
        for (SmartschoolGroupDTO group : groupInfo.groups()) {
            SchoolClass sc = classService.getClassByNameAndSchoolId(group.name(), defaultSchool.getId());
            if (sc == null) { sc = new SchoolClass(group.name(), defaultSchool, new ArrayList<>(List.of(user))); }
            else if (!sc.getUsers().contains(user)) { sc.getUsers().add(user); }
            classService.addClass(sc);
        }
    }

    private void linkOneRosterId(User user, SmartschoolGroupInfoResponseDTO groupInfo, SmartschoolUserInfo scInfo) {
        if (groupInfo == null || groupInfo.groups() == null || user == null || scInfo == null) return;

        OneRosterUserDTO[] oneRosterUsers = (user.getRole() == Role.LEERLING)
                ? oneRosterOAuthService.getAllStudents().users()
                : oneRosterOAuthService.getAllTeachers().users();

        List<OneRosterUserDTO> filteredOneRosterUsers = Arrays.stream(oneRosterUsers)
                .filter(orUser -> orUser.givenName().equals(scInfo.getName()) && orUser.familyName().equals(scInfo.getSurname()))
                .toList();

        if (filteredOneRosterUsers.size() == 1) {
            user.setOnerosterId(filteredOneRosterUsers.getFirst().sourcedId());
        } else if (filteredOneRosterUsers.size() > 1) {
            //If there are multiple users in OneRoster with the same name, check the classes they're in

            //Classes that the Smartschool user is in
            Set<String> ssGroupNames = Arrays.stream(groupInfo.groups())
                    .map(SmartschoolGroupDTO::name)
                    .collect(Collectors.toSet());

            //SourcedIds of the schools that the OneRoster users are in
            Set<String> schoolIds = filteredOneRosterUsers.stream()
                    .flatMap(u -> Arrays.stream(u.orgs()))
                    .map(OneRosterSchoolDTO::sourcedId)
                    .collect(Collectors.toSet());

            //SourcedIds of the OneRoster users
            Set<String> filteredSourcedIds = filteredOneRosterUsers.stream()
                    .map(OneRosterUserDTO::sourcedId)
                    .collect(Collectors.toSet());

            // Get the enrollments for each school, filter them by the sourcedIds of the OneRoster
            // users and check if the remaining enrollments are linked to a class that has the
            // same name as the class the Smartschool user is in, if so, link the user of that
            // enrollment to the Smartschool user's entity
            for (String schoolId : schoolIds) {
                OneRosterEnrollmentsResponseDTO enrollmentsResponse = oneRosterOAuthService.getEnrollmentsForSchool(schoolId);
                if (enrollmentsResponse == null || enrollmentsResponse.enrollments() == null) continue;

                Optional<OneRosterEnrollmentDTO> match = Arrays.stream(enrollmentsResponse.enrollments())
                        .filter(e -> filteredSourcedIds.contains(e.user().sourcedId()))
                        .filter(e -> ssGroupNames.contains(e.enrollmentClass().title()))
                        .findFirst();

                if (match.isPresent()) {
                    user.setOnerosterId(match.get().user().sourcedId());
                    return;
                }
            }
        }
    }

    private void redirectToLogin(HttpServletResponse response, String reason) throws IOException {
        response.sendRedirect(frontendBaseUrl + "/login?oauth=" + reason);
    }

    private void redirectToFrontend(HttpServletResponse response, String jwt, String refreshToken) throws IOException {
        response.sendRedirect(frontendBaseUrl + "/oauth-callback?token=" + url(jwt) + "&refreshToken=" + url(refreshToken));
    }

    private String platformUrl(String path) { return HTTPS + platform + path; }

    private boolean isHttpsRequest(HttpServletRequest request) {
        String fwd = request.getHeader("X-Forwarded-Proto");
        return request.isSecure() || "https".equalsIgnoreCase(fwd);
    }

    private String getEffectiveRedirectUri(HttpServletRequest request) {
        if (!isHttpsRequest(request)) return redirectUri;
        return UriComponentsBuilder.fromUriString(redirectUri).scheme("https").build(true).toUriString();
    }

    private Role mapSmartschoolRole(String baseRole) {
        if (baseRole == null) return Role.LEERLING;
        String r = baseRole.trim().toLowerCase();
        if (r.contains("leerkracht") || r.contains("leraar") || r.contains("personeel")) return Role.LEERKRACHT;
        return Role.LEERLING;
    }

    private String generateState() {
        byte[] bytes = new byte[32]; secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateRefreshToken() { return UUID.randomUUID().toString(); }
    private static String url(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }

    private static String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) if (name.equals(c.getName())) return c.getValue();
        return null;
    }
}