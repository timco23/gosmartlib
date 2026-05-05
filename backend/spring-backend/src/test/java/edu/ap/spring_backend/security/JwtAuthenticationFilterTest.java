package edu.ap.spring_backend.security;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.User;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(jwtService, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User user(int id, Role role) {
        User u = mock(User.class);
        when(u.getId()).thenReturn(id);
        when(u.getRole()).thenReturn(role);
        when(u.getEmail()).thenReturn("user@example.com");
        when(u.getName()).thenReturn("Doe");
        when(u.getFirstName()).thenReturn("Jane");
        return u;
    }

    @Test
    void givenNoAuthHeader_whenFilter_thenNoAuthenticationSet() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void givenDevToken_whenFilter_thenAuthenticationSet() throws Exception {
        User dev = user(1, Role.LEERLING);

        when(userRepository.findByEmail("leerling.dev@gsmartlib.tech")).thenReturn(Optional.of(dev));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer DEV_STUDENT_TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("1", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(dev.getRole().name())));
    }

    @Test
    void givenJwtToken_whenFilter_thenAuthenticationSet() throws Exception {
        User u = user(2, Role.ADMIN);


       when(jwtService.getUserIdFromToken("jwt")).thenReturn(2);
        when(userRepository.findById(2)).thenReturn(Optional.of(u));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("2", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(u.getRole().name())));
    }

    @Test
    void givenInvalidToken_whenFilter_thenNoAuthenticationSet() throws Exception {
        when(jwtService.getUserIdFromToken("bad")).thenThrow(new RuntimeException("bad"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}