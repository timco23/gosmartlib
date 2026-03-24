package edu.ap.spring_backend.specification;

import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.SchoolItemCopy;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

// Specifications replace the previous long complicated @Query with many optional filters
// it builds queries dynamically, when new filters are needed they can be added here as additional methods
public class ItemSpecifications {

    public static Specification<Item> withSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("author")), pattern)
            );
        };
    }

    public static Specification<Item> withLanguages(List<String> languages) {
        return (root, query, cb) -> {
            if (languages == null || languages.isEmpty()) return null;
            return root.get("bookLanguage").in(languages);
        };
    }

    public static Specification<Item> withMinPages(Integer minPages) {
        return (root, query, cb) -> minPages == null ? null :
                cb.greaterThanOrEqualTo(root.get("pageCount"), minPages);
    }

    public static Specification<Item> withMaxPages(Integer maxPages) {
        return (root, query, cb) -> maxPages == null ? null :
                cb.lessThanOrEqualTo(root.get("pageCount"), maxPages);
    }

    public static Specification<Item> withMinYear(Integer minYear) {
        return (root, query, cb) -> minYear == null ? null :
                cb.greaterThanOrEqualTo(root.get("releaseYear"), minYear);
    }

    public static Specification<Item> withMaxYear(Integer maxYear) {
        return (root, query, cb) -> maxYear == null ? null :
                cb.lessThanOrEqualTo(root.get("releaseYear"), maxYear);
    }

    public static Specification<Item> withMinScore(Double minScore) {
        return (root, query, cb) -> minScore == null ? null :
                cb.greaterThanOrEqualTo(root.get("reviewScore"), minScore);
    }

    // Filters Items by one or more tags using AND logic
    // a book must have ALL specified tags
    //
    // A separate subquery is created for each tag, fetching the IDs of
    // Items that have that tag. The main query retains only items
    // that appear in every subquery
    //
    // subqueries are used because MySQL does not allow ORDER BY
    // on columns absent from the SELECT list when DISTINCT is active
    public static Specification<Item> withTags(List<Long> tagIds) {
        return (root, query, cb) -> {
            if (tagIds == null || tagIds.isEmpty()) return null;

            Predicate[] predicates = tagIds.stream().map(tagId -> {
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<Item> subRoot = subquery.from(Item.class);
                Join<Object, Object> tags = subRoot.join("tags");

                subquery.select(subRoot.get("id"))
                        .where(cb.equal(tags.get("id"), tagId));

                return cb.in(root.get("id")).value(subquery);
            }).toArray(Predicate[]::new);

            return cb.and(predicates);
        };
    }

    public static Specification<Item> isFeatured(boolean featured) {
        return (root, query, cb) -> {
            Join<Item, SchoolItemCopy> copies = root.join("copies");
            query.distinct(true);
            return cb.equal(copies.get("featured"), featured);
        };
    }
}