package edu.ap.spring_backend.DAO;

import edu.ap.spring_backend.jpa.SchoolItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolItemRepository extends JpaRepository<SchoolItem,Integer> {
    Boolean existsByItemTitle(String itemTitle);
    Optional<SchoolItem> findByItemTitle(String itemTitle);

    // TODO - limit to a specific school
    Page<SchoolItem> findAll(Pageable pageable);

    // Optional filtered and sorted page based on user selection
    // - search: filters on title or author (case-insensitive, partial match)
    // - languages: filters on book language, ignored if empty, never NULL
    // - minPages/maxPages: filters on page count range
    // - minYear/maxYear: filters on release year range
    // - minScore: filters on minimum review score
    @Query("SELECT s FROM SchoolItem s WHERE " +
            "(:search IS NULL OR LOWER(s.item.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(s.item.author) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:#{#languages.isEmpty()} = true OR s.item.bookLanguage IN :languages) AND " +
            "(:minPages IS NULL OR s.item.pageCount >= :minPages) AND " +
            "(:maxPages IS NULL OR s.item.pageCount <= :maxPages) AND " +
            "(:minYear IS NULL OR s.item.releaseYear >= :minYear) AND " +
            "(:maxYear IS NULL OR s.item.releaseYear <= :maxYear) AND " +
            "(:minScore IS NULL OR s.item.reviewScore >= :minScore)")
    Page<SchoolItem> findWithFilters(
            @Param("search") String search,
            @Param("languages") List<String> languages,
            @Param("minPages") Integer minPages,
            @Param("maxPages") Integer maxPages,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("minScore") Double minScore,
            Pageable pageable);


}
