package edu.ap.spring_backend.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.ap.spring_backend.jpa.Campus;

/**
 * Repository interface for Campus entity.
 */
@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {
}