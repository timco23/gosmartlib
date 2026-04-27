package edu.ap.spring_backend.security;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Spring Security filter that authenticates requests using JWT or dev tokens.
 * Sets the authentication in the SecurityContext if a valid user is found.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * Filters incoming requests and sets authentication if a valid token is provided.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        Optional<User> userOpt = findDevUserByToken(token);
        if (userOpt.isEmpty()) {
            userOpt = findUserByJwtToken(token);
        }

        userOpt.ifPresent(user -> setAuthentication(user, request));

        filterChain.doFilter(request, response);
    }

    private Optional<User> findUserByJwtToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            Integer userId = jwtService.getUserIdFromToken(token);
            return userRepository.findById(userId);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Optional<User> findDevUserByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String email;
        switch (token) {
            case "DEV_ADMIN_TOKEN":
                email = "admin@gsmartlib.tech";
                break;
            case "DEV_TEACHER_TOKEN":
                email = "leerkracht.dev@gsmartlib.tech";
                break;
            case "DEV_BIBLIOTHEEKBEHEERDER_TOKEN":
                email = "bibliotheekbeheerder.dev@gsmartlib.tech";
                break;
            case "DEV_STUDENT_TOKEN":
                email = "leerling.dev@gsmartlib.tech";
                break;
            default:
                return Optional.empty();
        }

        return userRepository.findByEmail(email);
    }

    /**
     * Creates and sets a Spring Security Authentication object for the user.
     */
    private void setAuthentication(User user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(user.getId()),
                        null,
                        List.of(new SimpleGrantedAuthority(user.getRole().name()))
                );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}