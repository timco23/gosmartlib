package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository for {@link Review} persistence operations.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * Finds all reviews for a specific item.
     * @param itemId id of the item
     * @return list of reviews for the item
     */
    List<Review> findByItemId(Long itemId);

    /**
     * Deletes all reviews associated with a specific item ID.
     * @param itemId the ID of the item whose reviews should be deleted
     * @return the number of reviews deleted
     */
    long deleteByItem_Id(Long itemId);
}