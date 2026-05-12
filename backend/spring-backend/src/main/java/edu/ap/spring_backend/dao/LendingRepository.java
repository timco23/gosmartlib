package edu.ap.spring_backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.ap.spring_backend.dto.ItemCountDTO;
import edu.ap.spring_backend.jpa.Lending;

@Repository
public interface LendingRepository extends JpaRepository<Lending, Long> {
    boolean existsByBook_Id(int copyId);
    List<Lending> findByBorrowerEmail(String email);
    List<Lending> findByBook_Item_Id(Long itemId);
    List<Lending> findByBorrower_Id(Long userId);
    List<Lending> findByBook_Item_IdAndReturnedFalse(Long itemId);
    List<Lending> findByBook_Item_IdAndSchool_Id(Long itemId, Long schoolId);

    @Query("SELECT new edu.ap.spring_backend.dto.ItemCountDTO(l.book.item.id, COUNT(l)) FROM Lending l WHERE l.school.id = :schoolId GROUP BY l.book.item.id")
    List<ItemCountDTO> countLendingsByItemForSchool(@Param("schoolId") Long schoolId);

    @Query("SELECT new edu.ap.spring_backend.dto.ItemCountDTO(l.book.item.id, COUNT(l)) FROM Lending l WHERE l.school.id = :schoolId AND l.book.item.readingLevel.id = :readingLevelId GROUP BY l.book.item.id")
    List<ItemCountDTO> countLendingsByItemForSchoolAndReadingLevel(@Param("schoolId") Long schoolId, @Param("readingLevelId") Long readingLevelId);
}