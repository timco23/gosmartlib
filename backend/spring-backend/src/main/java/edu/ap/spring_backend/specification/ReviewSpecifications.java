package edu.ap.spring_backend.specification;

import edu.ap.spring_backend.jpa.Review;
import org.springframework.data.jpa.domain.Specification;

public class ReviewSpecifications {

    public static Specification<Review> withAuthorSchoolId(Long schoolId) {
        return (root, query, cb) -> schoolId == null ? null :
                cb.equal(root.get("author").get("school").get("id"), schoolId);
    }

    public static Specification<Review> withItemId(Long itemId) {
        return (root, query, cb) -> itemId == null ? null :
                cb.equal(root.get("item").get("id"), itemId);
    }

    public static Specification<Review> withAuthorId(Integer userId) {
        return (root, query, cb) -> userId == null ? null :
                cb.equal(root.get("author").get("id"), userId);
    }

    public static Specification<Review> withReportStatus(String reportStatus) {
        return (root, query, cb) -> {
            if (reportStatus == null || reportStatus.isBlank() || "ALL".equalsIgnoreCase(reportStatus)) {
                return null;
            }
            if ("REPORTED".equalsIgnoreCase(reportStatus)) {
                return cb.greaterThan(root.get("reportCount"), 0);
            }
            if ("NOT_REPORTED".equalsIgnoreCase(reportStatus)) {
                return cb.equal(root.get("reportCount"), 0);
            }
            return null;
        };
    }
}
