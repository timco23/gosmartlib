package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByItemId(Long itemId);
    long deleteByItem_Id(Long itemId);
    boolean existsByItemIdAndAuthorId(Long itemId, Integer authorId);
    long countByAuthor_Id(Integer userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.author.school.id = :schoolId")
    long countByAuthor_School_Id(@Param("schoolId") Long schoolId);
}