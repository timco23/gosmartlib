package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    boolean existsByReviewIdAndReporterId(Long reviewId, Integer reporterId);
    long countByReviewId(Long reviewId);
    List<ReviewReport> findByReviewId(Long reviewId);
    void deleteByReviewId(Long reviewId);
}