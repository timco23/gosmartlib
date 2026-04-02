package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dto.CreateReviewDTO;
import edu.ap.spring_backend.dto.ReviewDTO;
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

import java.util.List;

/**
 * REST controller for managing reviews on library items (books).
 * Base path: {/books/{itemId}/reviews}
 */
@RestController
@RequestMapping("/books/{itemId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Constructs the controller with required service.
     * @param reviewService service handling review logic
     */
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Retrieves all reviews for a given item.
     * @param itemId the item ID
     * @return list of reviews
     */
    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getReviews(@PathVariable Long itemId) {
        return ResponseEntity.ok(reviewService.getReviews(itemId));
    }

    /**
     * Creates a new review for a given item.
     * @param itemId the item ID
     * @param dto review data
     * @param authentication current authenticated user
     * @return created review
     */
     @PostMapping
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
    @DeleteMapping("/{reviewId}")
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
     @DeleteMapping
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
     * Resolves the email/identifier of the current requester.
     * Uses Spring Security authentication if available,
     * otherwise falls back to a Bearer token (development only).
     * @param authentication current authentication object
     * @param request HTTP request
     * @return email of the requester
     * @throws ResponseStatusException if the user is not authenticated
     */
   private String resolveRequester(Authentication authentication, HttpServletRequest request) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            return switch (token) {
                case "DEV_ADMIN_TOKEN" -> "admin@gsmartlib.tech";
                case "DEV_STUDENT_TOKEN" -> "leerling.dev@gsmartlib.tech";
                case "DEV_TEACHER_TOKEN" -> "leerkracht.dev@gsmartlib.tech";
                case "DEV_BIBLIOTHEEKBEHEERDER_TOKEN" -> "bibliotheekbeheerder.dev@gsmartlib.tech";
                default -> throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niet aangemeld");
            };
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niet aangemeld");
    }
}