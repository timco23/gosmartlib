package edu.ap.spring_backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.ap.spring_backend.jpa.SchoolItemCopy;

@Repository
public interface SchoolItemCopyRepository extends JpaRepository<SchoolItemCopy, Integer> {

    List<SchoolItemCopy> findByItemId(int itemId);
    boolean existsByItemIdAndSchoolId(int itemId, Long schoolId);
    boolean existsByItemTitleAndSchoolId(String title, Long schoolId);
    Optional<SchoolItemCopy> findByItemTitle(String title);
}