package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.ReviewRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.CreateReviewDTO;
import edu.ap.spring_backend.dto.ReviewDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.Review;
import edu.ap.spring_backend.jpa.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service containing business logic for item reviews.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ProfanityFilterService profanityFilterService;

    /**
     * Creates a new {@link ReviewService}.
     * @param reviewRepository repository for reviews
     * @param itemRepository repository for items
     * @param userRepository repository for users
     * @param profanityFilterService service for bad words
     */
    public ReviewService(ReviewRepository reviewRepository, ItemRepository itemRepository,
            UserRepository userRepository, ProfanityFilterService profanityFilterService) {
        this.reviewRepository = reviewRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.profanityFilterService = profanityFilterService;
    }

    /**
     * Returns all reviews for one item.
     * @param itemId id of the item
     * @param userEmail email of the requesting user (must exist)
     * @return list of review DTOs
     */
    public List<ReviewDTO> getReviewsForItem(Long itemId, String userEmail) {
        userRepository.findByEmail(userEmail).orElseThrow(EntityNotFoundException::new);
        return reviewRepository.findByItemId(itemId).stream().map(this::mapToDTO).toList();
    }

    /**
     * Returns all reviews.
     * @param itemId id of the item
     * @return list of review DTOs
     */
    public List<ReviewDTO> getReviews(Long itemId) {
        return reviewRepository.findByItemId(itemId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Creates a new review for an item and updates item review statistics.
     * Validates review content for profanity before saving.
     * @throws ResponseStatusException if profanity is detected
     * @param itemId id of the item
     * @param dto review input data
     * @param userEmail email of the review author
     * @return created review DTO
     */
    @Transactional
    public ReviewDTO createReview(Long itemId, CreateReviewDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(EntityNotFoundException::new);
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);

        String title = hasText(dto.title()) ? dto.title().trim() : null;
        String description = hasText(dto.description()) ? dto.description().trim() : null;

        if (profanityFilterService.containsProfanity(dto.title())
                || profanityFilterService.containsProfanity(dto.description())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review bevat ongepaste woorden.");
        }
        Review review = new Review(title, description, dto.stars(), item, user);
        reviewRepository.save(review);

        updateItemReviewStats(item);

        return mapToDTO(review);
    }

    /**
     * Deletes a review by id and updates item review statistics.
     * Only the review owner or users with ADMIN/BIBLIOTHEEKBEHEERDER role
     * are allowed to delete the review.
     * @param itemId id of the item
     * @param reviewId id of the review
     * @param requesterEmail email of the user that wants to delete the review
     */
    @Transactional
    public void deleteReview(Long itemId, Long reviewId, String requesterEmail) {
        User actor = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review niet gevonden"));

        if (!review.getItem().getId().equals(itemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review hoort niet bij dit item");
        }

        boolean isOwner = review.getAuthor() != null && review.getAuthor().getId().equals(actor.getId());
        String role = String.valueOf(actor.getRole());
        boolean isAdminOrManager = "ADMIN".equals(role) || "BIBLIOTHEEKBEHEERDER".equals(role);

        if (!isOwner && !isAdminOrManager) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Geen rechten om deze review te verwijderen");
        }

        Item item = review.getItem();
        reviewRepository.delete(review);
        updateItemReviewStats(item);
    }

    /**
     * Deletes all reviews for an item and resets item review statistics.
     * Only users with ADMIN or BIBLIOTHEEKBEHEERDER role can perform this action.
     * @param itemId id of the item
     * @param requesterEmail email of the user that wants to delete the review
     */
    @Transactional
    public void deleteAllReviewsForItem(Long itemId, String requesterEmail) {
        User actor = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden"));

        String role = String.valueOf(actor.getRole());
        boolean isAdminOrManager = "ADMIN".equals(role) || "BIBLIOTHEEKBEHEERDER".equals(role);

        if (!isAdminOrManager) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Geen rechten om alle reviews te verwijderen");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item niet gevonden"));

        reviewRepository.deleteByItem_Id(itemId);
        updateItemReviewStats(item);
    }

    /**
     * Recalculates and stores the review count and average score for an item.
     * @param item item to update
     */
    private void updateItemReviewStats(Item item) {
        List<Review> allReviews = reviewRepository.findByItemId(item.getId());
        int count = allReviews.size();
        double score = count == 0 ? 0 :
                allReviews.stream().mapToInt(Review::getStars).average().orElse(0);

        item.setReviewCount(count);
        item.setReviewScore(Math.round(score * 10.0) / 10.0);
        itemRepository.save(item);
    }

    /**
     * Maps a {@link Review} entity to a {@link ReviewDTO}.
     * @param review review entity
     * @return mapped review DTO
     */
    private ReviewDTO mapToDTO(Review review) {
        String authorName = review.getAuthor().getFirstName() != null
                ? review.getAuthor().getFirstName() + " " + review.getAuthor().getName()
                : review.getAuthor().getName();

        return new ReviewDTO(
                review.getId(),
                review.getTitle(),
                review.getDescription(),
                review.getStars(),
                review.getCreatedAt(),
                authorName
        );
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}