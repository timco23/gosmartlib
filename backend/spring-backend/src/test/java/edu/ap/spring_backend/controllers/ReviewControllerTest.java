package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.CreateReviewDTO;
import edu.ap.spring_backend.dto.CreateReviewReportDTO;
import edu.ap.spring_backend.dto.ReviewDTO;
import edu.ap.spring_backend.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewControllerTest {

    @Mock private ReviewService reviewService;
    private ReviewController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ReviewController(reviewService);
    }

    private static String jwtWithEmail(String email) {
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"email\":\"" + email + "\"}").getBytes(StandardCharsets.UTF_8));
        return "x." + payload + ".y";
    }

    @Test
    void givenItemId_whenGetReviews_thenReturnsList() {
        ReviewDTO dto = new ReviewDTO(1L, "Great", 5, LocalDateTime.now(),
                "Author", "a@b.com", "ss", false, 0, false, 1L, "book");
        when(reviewService.getReviews(1L)).thenReturn(List.of(dto));

        ResponseEntity<List<ReviewDTO>> response = controller.getReviews(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(reviewService).getReviews(1L);
    }

    @Test
    void givenAuthentication_whenCreateReview_thenUsesAuthName() {
        CreateReviewDTO dto = new CreateReviewDTO("Nice", 4, false);
        ReviewDTO created = new ReviewDTO(1L, "Nice", 4, LocalDateTime.now(),
                "Author", "a@b.com", "ss", false, 0, false, 1L, "book");
        when(reviewService.createReview(1L, dto, "user@example.com")).thenReturn(created);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user@example.com");

        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<ReviewDTO> response = controller.createReview(1L, dto, auth, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(created, response.getBody());
        verify(reviewService).createReview(1L, dto, "user@example.com");
    }

    @Test
    void givenDevToken_whenCreateReview_thenUsesDevEmail() {
        CreateReviewDTO dto = new CreateReviewDTO("Nice", 4, false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer DEV_STUDENT_TOKEN");

        controller.createReview(1L, dto, null, request);

        verify(reviewService).createReview(1L, dto, "leerling.dev@gsmartlib.tech");
    }

    @Test
    void givenJwtToken_whenCreateReview_thenUsesEmailClaim() {
        CreateReviewDTO dto = new CreateReviewDTO("Nice", 4, false);
        String token = jwtWithEmail("jwt@example.com");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        controller.createReview(1L, dto, null, request);

        verify(reviewService).createReview(1L, dto, "jwt@example.com");
    }

    @Test
    void givenNoAuth_whenCreateReview_thenUnauthorized() {
        CreateReviewDTO dto = new CreateReviewDTO("Nice", 4, false);

        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createReview(1L, dto, null, request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void givenAuth_whenDeleteReview_thenCallsService() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user@example.com");

        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<Void> response = controller.deleteReview(1L, 10L, auth, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reviewService).deleteReview(1L, 10L, "user@example.com");
    }

    @Test
    void givenAuth_whenReportReview_thenCreated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user@example.com");

        MockHttpServletRequest request = new MockHttpServletRequest();
        CreateReviewReportDTO dto = new CreateReviewReportDTO("spam");

        ResponseEntity<Void> response = controller.reportReview(1L, 10L, dto, auth, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(reviewService).reportReview(1L, 10L, dto, "user@example.com");
    }
}