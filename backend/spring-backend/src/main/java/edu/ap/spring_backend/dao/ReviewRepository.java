package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByItemId(Long itemId);
    long deleteByItem_Id(Long itemId);
    long countByAuthor_Id(Integer userId);
}