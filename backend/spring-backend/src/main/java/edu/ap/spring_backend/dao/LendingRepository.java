package edu.ap.spring_backend.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.Lending;

@Repository
public interface LendingRepository extends ListCrudRepository<Lending, Long> {
    boolean existsByBook_Id(int copyId);
    List<Lending> findByBorrowerEmail(String email);
    List<Lending> findByBook_Item_Id(Long itemId);
    List<Lending> findByBorrower_Id(Long userId);
    List<Lending> findByBook_Item_IdAndReturnedFalse(Long itemId);

    //For the most frequently loaned book
    @Query("""
        SELECT l.book.item FROM Lending l
        WHERE l.school.id = :schoolId
        AND l.lendingDate >= :start
        AND l.lendingDate <= :end
        GROUP BY l.book.item
        ORDER BY COUNT(l) DESC
        LIMIT 1
    """)
    Optional<Item> findMostBorrowedThisMonth(
        @Param("schoolId") Long schoolId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    Optional<Lending> findTopByBorrower_IdOrderByLendingDateDesc(Integer borrowerId);
}