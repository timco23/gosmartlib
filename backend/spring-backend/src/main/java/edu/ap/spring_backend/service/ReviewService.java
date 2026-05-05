package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.ReviewReportRepository;
import edu.ap.spring_backend.dao.ReviewRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.CreateReviewDTO;
import edu.ap.spring_backend.dto.CreateReviewReportDTO;
import edu.ap.spring_backend.dto.ReviewDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.Review;
import edu.ap.spring_backend.jpa.ReviewReport;
import edu.ap.spring_backend.jpa.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.web.server.ResponseStatusException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Service containing business logic for item reviews.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ProfanityFilterService profanityFilterService;
    private final ReviewReportRepository reviewReportRepository;

    /**
     * Creates a new {@link ReviewService}.
     * @param reviewRepository repository for reviews
     * @param itemRepository repository for items
     * @param userRepository repository for users
     * @param profanityFilterService service for bad words
     * @param reviewReportRepository repository reported reviews
     */
    public ReviewService(ReviewRepository reviewRepository, ItemRepository itemRepository,
            UserRepository userRepository, ProfanityFilterService profanityFilterService,  ReviewReportRepository reviewReportRepository) {
        this.reviewRepository = reviewRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.profanityFilterService = profanityFilterService;
        this.reviewReportRepository = reviewReportRepository;
    }

    /**
     * Resolves a user based on different identifier formats.
     * @param identifier email, username or ID
     * @return resolved User
     * @throws ResponseStatusException if user is not found
     */
    private User resolveUser(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden");
        }

        Set<String> candidates = new LinkedHashSet<>();
        String raw = identifier.trim();
        candidates.add(raw);
        candidates.add(raw.toLowerCase(Locale.ROOT));

        if (raw.contains("|")) {
            String part = raw.substring(raw.lastIndexOf('|') + 1).trim();
            if (!part.isBlank()) {
                candidates.add(part);
                candidates.add(part.toLowerCase(Locale.ROOT));
            }
        }

        if (raw.contains("\\")) {
            String part = raw.substring(raw.lastIndexOf('\\') + 1).trim();
            if (!part.isBlank()) {
                candidates.add(part);
                candidates.add(part.toLowerCase(Locale.ROOT));
            }
        }

        if (raw.contains("@")) {
            String localPart = raw.substring(0, raw.indexOf('@')).trim();
            if (!localPart.isBlank()) {
                candidates.add(localPart);
                candidates.add(localPart.toLowerCase(Locale.ROOT));
            }
        }

        for (String c : candidates) {
            var byEmail = userRepository.findByEmailIgnoreCase(c);
            if (byEmail.isPresent()) return byEmail.get();

            var bySs = userRepository.findBySmartschoolUsernameIgnoreCase(c);
            if (bySs.isPresent()) return bySs.get();
        }

        for (String c : candidates) {
            if (c.matches("\\d+")) {
                var byId = userRepository.findById(Integer.parseInt(c));
                if (byId.isPresent()) return byId.get();
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden");
    }

    /**
     * Returns all reviews for one item.
     * @param itemId id of the item
     * @param userEmail email of the requesting user (must exist)
     * @return list of review DTOs
     */
    public List<ReviewDTO> getReviewsForItem(Long itemId, String userEmail) {
        resolveUser(userEmail);
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
        User user = resolveUser(userEmail);
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);

        String description = hasText(dto.description()) ? dto.description().trim() : null;

         if (profanityFilterService.containsProfanity(description)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review bevat ongepaste woorden.");
        }

        Review review = new Review(description, dto.stars(), item, user, dto.isAnonymous());
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
        User actor = resolveUser(requesterEmail);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review niet gevonden"));

        if (!review.getItem().getId().equals(itemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review hoort niet bij dit item");
        }

        boolean isOwner = review.getAuthor() != null && isSameUser(review.getAuthor(), actor);
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
     * Allows a user to report a review.
     * Validates that the review exists, belongs to the given item, is not owned by the reporter,
     * has not been approved and has not already been reported by the same user. Updates the report count accordingly.
     * @param itemId id of the item the review belongs to
     * @param reviewId id of the review to report
     * @param dto contains the reason for reporting
     * @param requesterEmail email of the reporting user
     * @throws ResponseStatusException if validation fails
     */
    @Transactional
    public void reportReview(Long itemId, Long reviewId, CreateReviewReportDTO dto, String requesterEmail) {
        User reporter = resolveUser(requesterEmail);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review niet gevonden"));

        if (!review.getItem().getId().equals(itemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review hoort niet bij dit item");
        }

        if (review.getAuthor() != null && isSameUser(review.getAuthor(), reporter)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Eigen review kan niet gerapporteerd worden");
        }

        if (reviewReportRepository.existsByReviewIdAndReporterId(reviewId, reporter.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review is al gerapporteerd door deze gebruiker");
        }

        if (review.isApproved()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Review is goedgekeurd door een bibliotheekbeheerder");
        }

        ReviewReport rr = new ReviewReport();
        rr.setReview(review);
        rr.setReporter(reporter);
        rr.setAutoGenerated(false);
        rr.setReason(hasText(dto.reason()) ? dto.reason().trim() : null);
        reviewReportRepository.save(rr);

        long count = reviewReportRepository.countByReviewId(reviewId);
        review.setReportCount((int) count);
        reviewRepository.save(review);
    }

    private boolean isSameUser(User a, User b) {
        if (a == null || b == null) return false;
        if (a.getId() != null && a.getId().equals(b.getId())) return true;
        if (equalsIgnoreCaseSafe(a.getEmail(), b.getEmail())) return true;
        return equalsIgnoreCaseSafe(a.getSmartschoolUsername(), b.getSmartschoolUsername());
    }

    private boolean equalsIgnoreCaseSafe(String x, String y) {
        return x != null && y != null && x.trim().equalsIgnoreCase(y.trim());
    }

    /**
     * Deletes all reviews for an item and resets item review statistics.
     * Only users with ADMIN or BIBLIOTHEEKBEHEERDER role can perform this action.
     * @param itemId id of the item
     * @param requesterEmail email of the user that wants to delete the review
     */
    @Transactional
    public void deleteAllReviewsForItem(Long itemId, String requesterEmail) {
        User actor = resolveUser(requesterEmail);

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

        // The backend ALWAYS passes the real name
        // The frontend determines what is displayed based on isAnonymous and userRole
        return new ReviewDTO(
                review.getId(),
                review.getDescription(),
                review.getStars(),
                review.getCreatedAt(),
                authorName,
                review.getAuthor().getEmail(),
                review.getAuthor().getSmartschoolUsername(),
                review.isAnonymous(),
                review.getReportCount()
        );
    }

    /**
     * Checks if a string contains non-whitespace text.
     * @param s string to check
     * @return true if not null and not empty
     */
    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}