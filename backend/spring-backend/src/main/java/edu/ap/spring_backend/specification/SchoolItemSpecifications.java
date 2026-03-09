package edu.ap.spring_backend.specification;

import edu.ap.spring_backend.jpa.SchoolItem;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

// Specifications replace the previous long complicated @Query with many optional filters
// it builds queries dynamically, when new filters are needed they can be added here as additional methods
public class SchoolItemSpecifications {

    public static Specification<SchoolItem> withSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("item").get("title")), pattern),
                    cb.like(cb.lower(root.get("item").get("author")), pattern)
            );
        };
    }

    public static Specification<SchoolItem> withLanguages(List<String> languages) {
        return (root, query, cb) -> {
            if (languages == null || languages.isEmpty()) return null;
            return root.get("item").get("bookLanguage").in(languages);
        };
    }

    public static Specification<SchoolItem> withMinPages(Integer minPages) {
        return (root, query, cb) -> minPages == null ? null :
                cb.greaterThanOrEqualTo(root.get("item").get("pageCount"), minPages);
    }

    public static Specification<SchoolItem> withMaxPages(Integer maxPages) {
        return (root, query, cb) -> maxPages == null ? null :
                cb.lessThanOrEqualTo(root.get("item").get("pageCount"), maxPages);
    }

    public static Specification<SchoolItem> withMinYear(Integer minYear) {
        return (root, query, cb) -> minYear == null ? null :
                cb.greaterThanOrEqualTo(root.get("item").get("releaseYear"), minYear);
    }

    public static Specification<SchoolItem> withMaxYear(Integer maxYear) {
        return (root, query, cb) -> maxYear == null ? null :
                cb.lessThanOrEqualTo(root.get("item").get("releaseYear"), maxYear);
    }

    public static Specification<SchoolItem> withMinScore(Double minScore) {
        return (root, query, cb) -> minScore == null ? null :
                cb.greaterThanOrEqualTo(root.get("item").get("reviewScore"), minScore);
    }

    // Filters SchoolItems by one or more tags using AND logic
    // a book must have ALL specified tags
    //
    // A separate subquery is created for each tag, fetching the IDs of
    // SchoolItems that have that tag. The main query retains only items
    // that appear in every subquery
    //
    // subqueries are used because MySQL does not allow ORDER BY
    // on columns absent from the SELECT list when DISTINCT is active
    public static Specification<SchoolItem> withTags(List<Long> tagIds) {
        return (root, query, cb) -> {
            if (tagIds == null || tagIds.isEmpty()) return null;

            Predicate[] predicates = tagIds.stream().map(tagId -> {
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<SchoolItem> subRoot = subquery.from(SchoolItem.class);
                Join<Object, Object> tags = subRoot.join("tags");

                subquery.select(subRoot.get("id"))
                        .where(cb.equal(tags.get("id"), tagId));

                return cb.in(root.get("id")).value(subquery);
            }).toArray(Predicate[]::new);

            return cb.and(predicates);
        };
    }
}