package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.SmartschoolUserInfo;
import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.security.JwtService;
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
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

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

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final SecureRandom secureRandom = new SecureRandom();
    private final JwtService jwtService;

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

     /**
     * Creates the controller and initializes {@link JwtService} using application properties.
     * @param userRepository repository for loading/saving users
     * @param jwtSecret JWT signing secret
     * @param expiresMinutes JWT lifetime in minutes
     */
    public SmartschoolOAuthController(
            UserRepository userRepository,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expires-minutes}") long expiresMinutes
    ) {
        this.userRepository = userRepository;
        this.jwtService = new JwtService(jwtSecret, expiresMinutes);
    }

    /**
     * Starts the OAuth flow.
     * Generates a random state value, stores it in an HttpOnly cookie, and redirects to Smartschool.
     * @param response HTTP response used to set cookie and redirect
     */
   @GetMapping("/oauth/smartschool/login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String state = generateState();

        boolean https = isHttpsRequest(request);

        // Build cookie correctly (instead of manual Set-Cookie string + unused Cookie object)
        ResponseCookie stateCookie = ResponseCookie.from("oauth_state", state)
                .httpOnly(true)
                .secure(https)          // true in HTTPS/proxy, false for plain local HTTP
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, stateCookie.toString());

        String authorizeUrl = "https://" + platform + "/OAuth";
        String effectiveRedirectUri = getEffectiveRedirectUri(request);

        String redirect = authorizeUrl
                + "?response_type=code"
                + "&client_id=" + url(clientId)
                + "&redirect_uri=" + url(effectiveRedirectUri)
                + "&scope=" + url("userinfo")
                + "&state=" + url(state);

        response.setStatus(302);
        response.setHeader("Location", redirect);
    }

    /**
     * Handles the OAuth callback from Smartschool.
     * Validates the state cookie, exchanges the code for an access token, fetches user info,
     * finds/creates a local user, generates a JWT, and redirects to the frontend with the token.
     * @param request incoming request containing "code" and "state"
     * @param response HTTP response used for redirects
     */
    @GetMapping("/oauth/smartschool/callback")
    public void callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String code = request.getParameter("code");
        String state = request.getParameter("state");

        if (code == null || state == null) {
            response.sendRedirect(frontendBaseUrl + "/login?oauth=error");
            return;
        }

        String cookieState = readCookie(request, "oauth_state");
        if (cookieState == null || !cookieState.equals(state)) {
            response.sendRedirect(frontendBaseUrl + "/login?oauth=state_mismatch");
            return;
        }

        // 1) access token
        String tokenUrl = "https://oauth.smartschool.be/OAuth/index/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // This implementation sends client_id and client_secret in the request body
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", getEffectiveRedirectUri(request)); // must match login redirect_uri
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(form, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            response.sendRedirect(frontendBaseUrl + "/login?oauth=token_error");
            return;
        }

        Object accessTokenObj = tokenResponse.getBody().get("access_token");
        if (accessTokenObj == null) {
            // Some providers nest the token under result.access_token
            Object result = tokenResponse.getBody().get("result");
            if (result instanceof Map<?, ?> m && m.get("access_token") != null) {
                accessTokenObj = m.get("access_token");
            }
        }

        if (accessTokenObj == null) {
            response.sendRedirect(frontendBaseUrl + "/login?oauth=token_missing");
            return;
        }

        String accessToken = String.valueOf(accessTokenObj);

        // 2) Fetch user info using the access token
        String userInfoUrl = "https://oauth.smartschool.be/Api/V1/userinfo?access_token=" + url(accessToken);
        SmartschoolUserInfo info = restTemplate.getForObject(userInfoUrl, SmartschoolUserInfo.class);

        if (info == null) {
            response.sendRedirect(frontendBaseUrl + "/login?oauth=userinfo_error");
            return;
        }

        // 3) Find or create a local user based on Smartschool username
       User user = userRepository.findBySmartschoolUsername(info.username).orElse(null);

        if (user == null) {
            user = new User();
            user.setSmartschoolUsername(info.username);
            user.setFirstName(info.name);        // from Smartschool userinfo
            user.setName(info.surname);          // from Smartschool userinfo

            // Map Smartschool role to local role
            // Only new users get role from Smartschool; existing users keep their role
            Role mappedRole = mapSmartschoolRoleToLocalRole(info.role);
            user.setRole(mappedRole);

            // The database requires a non-null, unique email
            user.setEmail(info.username + "@smartschool.local");

            // The database requires a non-null password (not used for OAuth logins)
            user.setPassword("{noop}oauth"); 

            user = userRepository.save(user);
        }

        // Generate JWT (short-lived)
        String jwt = jwtService.generateToken(user);

        // Generate refresh token (long-lived) and save to database
        String refreshToken = generateRefreshToken();
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // 4) Redirect to the frontend with the JWT and refresh token
        String redirectUrl = frontendBaseUrl + "/oauth/callback?token=" + url(jwt) + "&refreshToken=" + url(refreshToken);
        response.sendRedirect(redirectUrl);
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
     *
     * @param smartschoolRole the role string from Smartschool
     * @return mapped local Role
     */
    private Role mapSmartschoolRoleToLocalRole(String smartschoolRole) {
        if (smartschoolRole == null) {
            return Role.LEERLING; // default
        }

        // Normalize to lowercase for comparison
        String normalized = smartschoolRole.toLowerCase().trim();

        if (normalized.contains("teacher") || normalized.contains("leerkracht") || normalized.contains("staff")) {
            return Role.LEERKRACHT;
        } else if (normalized.contains("student") || normalized.contains("leerling")) {
            return Role.LEERLING;
        }

        return Role.LEERLING; // default fallback
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