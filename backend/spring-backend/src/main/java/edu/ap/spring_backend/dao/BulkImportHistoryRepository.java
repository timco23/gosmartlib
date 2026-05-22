package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.BulkImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkImportHistoryRepository extends JpaRepository<BulkImportHistory, Long> {
    List<BulkImportHistory> findBySchoolIdOrderByImportedAtDesc(Long schoolId);
}
