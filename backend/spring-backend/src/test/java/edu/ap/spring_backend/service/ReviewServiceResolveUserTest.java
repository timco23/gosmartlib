package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.ReviewReportRepository;
import edu.ap.spring_backend.dao.ReviewRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.ReviewDTO;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceResolveUserTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProfanityFilterService profanityFilterService;
    @Mock private ReviewReportRepository reviewReportRepository;

    @InjectMocks private ReviewService reviewService;

    @Test
    void getReviewsForItem_resolvesByEmailIgnoreCase() {
        User u = new User();
        when(userRepository.findByEmailIgnoreCase("User@School.be")).thenReturn(Optional.of(u));
        when(reviewRepository.findByItemId(1L)).thenReturn(List.of());

        List<ReviewDTO> result = reviewService.getReviewsForItem(1L, "User@School.be");
        assertNotNull(result);
    }

    @Test
    void getReviewsForItem_resolvesBySmartschoolUsername() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        User u = new User();
        when(userRepository.findBySmartschoolUsernameIgnoreCase(anyString())).thenReturn(Optional.of(u));
        when(reviewRepository.findByItemId(1L)).thenReturn(List.of());

        List<ReviewDTO> result = reviewService.getReviewsForItem(1L, "DOMAIN\\user");
        assertNotNull(result);
    }

    @Test
    void getReviewsForItem_resolvesById() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userRepository.findBySmartschoolUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());

        User u = new User();
        ReflectionTestUtils.setField(u, "id", 123);
        when(userRepository.findById(123)).thenReturn(Optional.of(u));
        when(reviewRepository.findByItemId(1L)).thenReturn(List.of());

        List<ReviewDTO> result = reviewService.getReviewsForItem(1L, "123");
        assertNotNull(result);
    }
}