package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.CreateReviewDTO;
import edu.ap.spring_backend.dto.CreateReviewReportDTO;
import edu.ap.spring_backend.dto.ReviewDTO;
import edu.ap.spring_backend.dto.ReviewReportDTO;
import edu.ap.spring_backend.dto.PageResponseDTO;
import edu.ap.spring_backend.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.List;

/**
 * REST controller for managing reviews on library items (books).
 * Base path: {/reviews}
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Retrieves all reviews for a given item.
     * @param itemId the item ID
     * @return list of reviews
     */
    @GetMapping("/books/{itemId}")
    public ResponseEntity<List<ReviewDTO>> getReviews(@PathVariable Long itemId) {
        return ResponseEntity.ok(reviewService.getReviews(itemId));
    }

    /**
     * Checks if a user has reported a review with the corresponding review id.
     * @param reviewId id of the review
     * @return true if the user already reported the review
     */
    @GetMapping("/{reviewId}/existsFrom/me")
    public ResponseEntity<Boolean> reportExistsByReviewIdAndReporterId(
            @PathVariable Long reviewId,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String requester = resolveRequester(authentication, request);
        return ResponseEntity.ok(reviewService.reportExistsByReviewIdAndReporterId(reviewId, requester));
    }

    /**
     * Creates a new review for a given item.
     * @param itemId the item ID
     * @param dto review data
     * @param authentication current authenticated user
     * @return created review
     */
     @PostMapping("/books/{itemId}")
    public ResponseEntity<ReviewDTO> createReview(
            @PathVariable Long itemId,
            @Valid @RequestBody CreateReviewDTO dto,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String requester = resolveRequester(authentication, request);
        ReviewDTO created = reviewService.createReview(itemId, dto, requester);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Deletes a specific review for an item.
     * @param itemId the item ID
     * @param reviewId the review ID
     * @param authentication current authenticated user
     * @return no content response
     */
    @DeleteMapping("/{reviewId}/books/{itemId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long itemId,
            @PathVariable Long reviewId,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String requester = resolveRequester(authentication, request);
        reviewService.deleteReview(itemId, reviewId, requester);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all reviews for a given item.
     * @param itemId the item ID
     * @param authentication current authenticated user
     * @return no content response
     */
     @DeleteMapping("/books/{itemId}")
    public ResponseEntity<Void> deleteAllReviews(
            @PathVariable Long itemId,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String requester = resolveRequester(authentication, request);
        reviewService.deleteAllReviewsForItem(itemId, requester);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reports a review.
     * @param itemId the item ID
     * @param reviewId current review
     * @param authentication current authenticated user
     * @param dto the dto for creating a review report
     */
    @PostMapping("/{reviewId}/books/{itemId}/reports")
    public ResponseEntity<Void> reportReview(
            @PathVariable Long itemId,
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewReportDTO dto,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String requester = resolveRequester(authentication, request);
        reviewService.reportReview(itemId, reviewId, dto, requester);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

   /**
     * Resolves the email/identifier of the current requester.
     * Uses Spring Security authentication if available,
     * otherwise falls back to a Bearer token.
     * @param authentication current authentication object
     * @param request HTTP request
     * @return email of the requester
     * @throws ResponseStatusException if the user is not authenticated
     */
   private String resolveRequester(Authentication authentication, HttpServletRequest request) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String name = authentication.getName();
            if (name != null && !name.isBlank() && !"anonymousUser".equalsIgnoreCase(name)) {
                return name.trim();
            }
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niet aangemeld");
        }

        String token = header.substring(7).trim();
        return extractPrincipalFromJwt(token);
    }

    /**
     * Extracts the user identifier from a JWT token.
     * @param token JWT token
     * @return extracted identifier (e.g. email or username)
     * @throws ResponseStatusException if token is invalid
     */
    private String extractPrincipalFromJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ongeldige token");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);

            String value = firstNonBlank(
                    asString(payload.get("email")),
                    asString(payload.get("preferred_username")),
                    asString(payload.get("upn")),
                    asString(payload.get("smartschool_username")),
                    asString(payload.get("sub"))
            );

            if (value == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token zonder identiteit");
            }

            return value;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Ongeldige token");
        }
    }

    /**
     * Converts an object to a trimmed string.
     * @param value object to convert
     * @return trimmed string or null if value is null
     */
    private String asString(Object value) {
        return value == null ? null : value.toString().trim();
    }

    /**
     * Returns the first non-blank value from the given list.
     * @param values values to check
     * @return first non-blank value or null if none found
     */
    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    /**
     * Retrieves filtered and paged reviews for a school.
     *
     * @param schoolId     id of the school
     * @param itemId       optional item filter
     * @param userId       optional user filter
     * @param reportStatus optional report status filter (ALL, REPORTED, NOT_REPORTED)
     * @param page         page number (default 0)
     * @param newestFirst  if true, newest reviews first (within same reportCount); else oldest first
     * @return paged list of reviews
     */
    @GetMapping("/schools/{schoolId}")
    public ResponseEntity<PageResponseDTO<ReviewDTO>> getFilteredReviewsBySchoolId(
            @PathVariable Long schoolId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(defaultValue = "ALL") String reportStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "true") boolean newestFirst
    ) {
        return ResponseEntity.ok(reviewService.getFilteredReviewsBySchoolId(
                schoolId, itemId, userId, reportStatus, page, newestFirst));
    }

    /**
     * Retrieves all reports for a specific review.
     *
     * @param reviewId id of the review
     * @return list of review reports
     */
    @GetMapping("/{reviewId}/reports")
    public ResponseEntity<List<ReviewReportDTO>> getReviewReportsByReviewId(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewReportsByReviewId(reviewId));
    }

    /**
     * Approves a review, resetting its report count and making it unable to reported again.
     *
     * @param reviewId id of the review
     * @return no content
     */
    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<Void> approveReview(@PathVariable Long reviewId) {
        reviewService.approveReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    
}