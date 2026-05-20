package edu.ap.spring_backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.ap.spring_backend.dto.ItemCountDTO;
import edu.ap.spring_backend.jpa.SchoolItemCopy;

@Repository
public interface SchoolItemCopyRepository extends JpaRepository<SchoolItemCopy, Integer> {

    List<SchoolItemCopy> findByItemIdAndSchoolId(Long itemId, Long schoolId);
    boolean existsByItemTitleAndSchoolId(String title, Long schoolId);
    Optional<SchoolItemCopy> findByItemTitleAndSchoolId(String title,  Long schoolId);
    Optional<SchoolItemCopy> findByBarcode(String barcode);
    List<SchoolItemCopy> findTop50BySchoolIdOrderByIdDesc(Long schoolId);

    @Query("SELECT new edu.ap.spring_backend.dto.ItemCountDTO(c.item.id, COUNT(c)) FROM SchoolItemCopy c WHERE c.school.id = :schoolId GROUP BY c.item.id")
    List<ItemCountDTO> countCopiesByItemForSchool(@Param("schoolId") Long schoolId);

    @Query("SELECT DISTINCT c.item.id FROM SchoolItemCopy c WHERE c.school.id = :schoolId AND c.item.readingLevel.id = :readingLevelId")
    List<Long> findItemIdsBySchoolAndReadingLevel(@Param("schoolId") Long schoolId, @Param("readingLevelId") Long readingLevelId);
}