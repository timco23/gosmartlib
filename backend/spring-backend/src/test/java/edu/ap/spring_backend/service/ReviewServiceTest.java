package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.ReviewReportRepository;
import edu.ap.spring_backend.dao.ReviewRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.CreateReviewDTO;
import edu.ap.spring_backend.dto.CreateReviewReportDTO;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.Review;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProfanityFilterService profanityFilterService;
    @Mock private ReviewReportRepository reviewReportRepository;

    @InjectMocks private ReviewService service;

    private Item item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        item = new Item();
        item.setId(1L);
    }

    private User mockUser(int id, String email, String ssUser, Role role) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getEmail()).thenReturn(email);
        when(user.getSmartschoolUsername()).thenReturn(ssUser);
        when(user.getRole()).thenReturn(role);
        when(user.getName()).thenReturn("User");
        when(user.getFirstName()).thenReturn("Test");
        return user;
    }

    private Review reviewFor(Item item, User author, int stars) {
        Review review = new Review();
        review.setId(10L);
        review.setItem(item);
        review.setAuthor(author);
        review.setStars(stars);
        review.setDescription("Review");
        review.setReportCount(0);
        return review;
    }

    @Test
    void givenProfanity_whenCreateReview_thenBadRequest() {
        CreateReviewDTO dto = new CreateReviewDTO("bad words", 3, false);
        User user = mockUser(1, "user@example.com", "user", Role.LEERLING);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(profanityFilterService.containsProfanity("bad words")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createReview(1L, dto, "user@example.com"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void givenValidReview_whenCreateReview_thenSavesAndUpdatesStats() {
        CreateReviewDTO dto = new CreateReviewDTO("Nice", 4, false);
        User user = mockUser(1, "user@example.com", "user", Role.LEERLING);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(profanityFilterService.containsProfanity("Nice")).thenReturn(false);
        when(reviewRepository.findByItemId(1L)).thenReturn(List.of(reviewFor(item, user, 4)));

        service.createReview(1L, dto, "user@example.com");

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository, atLeastOnce()).save(itemCaptor.capture());
        Item saved = itemCaptor.getValue();

        assertEquals(1, saved.getReviewCount());
        assertEquals(4.0, saved.getReviewScore());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void givenOwner_whenDeleteReview_thenDeletesAndUpdatesStats() {
        User owner = mockUser(1, "user@example.com", "user", Role.LEERLING);
        Review review = reviewFor(item, owner, 5);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(owner));
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(reviewRepository.findByItemId(1L)).thenReturn(List.of());

        service.deleteReview(1L, 10L, "user@example.com");

        verify(reviewRepository).delete(review);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void givenNonOwnerNonAdmin_whenDeleteReview_thenForbidden() {
        User actor = mockUser(2, "actor@example.com", "actor", Role.LEERLING);
        User other = mockUser(1, "other@example.com", "other", Role.LEERLING);
        Review review = reviewFor(item, other, 5);

        when(userRepository.findByEmailIgnoreCase("actor@example.com")).thenReturn(Optional.of(actor));
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteReview(1L, 10L, "actor@example.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void givenWrongItem_whenDeleteReview_thenNotFound() {
        User owner = mockUser(1, "user@example.com", "user", Role.LEERLING);
        Item otherItem = new Item();
        otherItem.setId(99L);
        Review review = reviewFor(otherItem, owner, 5);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(owner));
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteReview(1L, 10L, "user@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void givenDuplicateReport_whenReportReview_thenConflict() {
        User reporter = mockUser(1, "user@example.com", "user", Role.LEERLING);
        User author = mockUser(2, "author@example.com", "author", Role.LEERLING);
        Review review = reviewFor(item, author, 4);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(reporter));
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(reviewReportRepository.existsByReviewIdAndReporterId(10L, 1)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.reportReview(1L, 10L, new CreateReviewReportDTO("spam"), "user@example.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(reviewReportRepository, never()).save(any());
    }

    @Test
    void givenValidReport_whenReportReview_thenUpdatesReportCount() {
        User reporter = mockUser(1, "user@example.com", "user", Role.LEERLING);
        User author = mockUser(2, "author@example.com", "author", Role.LEERLING);
        Review review = reviewFor(item, author, 4);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(reporter));
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
        when(reviewReportRepository.existsByReviewIdAndReporterId(10L, 1)).thenReturn(false);
        when(reviewReportRepository.countByReviewId(10L)).thenReturn(3L);

        service.reportReview(1L, 10L, new CreateReviewReportDTO("spam"), "user@example.com");

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertEquals(3, captor.getValue().getReportCount());
        verify(reviewReportRepository).save(any());
    }

    @Test
    void givenNonAdmin_whenDeleteAllReviews_thenForbidden() {
        User actor = mockUser(1, "user@example.com", "user", Role.LEERLING);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(actor));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteAllReviewsForItem(1L, "user@example.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void givenAdmin_whenDeleteAllReviews_thenDeletesAndResetsStats() {
        User admin = mockUser(1, "admin@example.com", "admin", Role.ADMIN);

        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(reviewRepository.findByItemId(1L)).thenReturn(List.of());

        service.deleteAllReviewsForItem(1L, "admin@example.com");

        verify(reviewRepository).deleteByItem_Id(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void givenUnknownUser_whenGetReviewsForItem_thenUnauthorized() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userRepository.findBySmartschoolUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getReviewsForItem(1L, "unknown"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}