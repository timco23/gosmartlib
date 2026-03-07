package edu.ap.spring_backend.DAO;

import edu.ap.spring_backend.jpa.SchoolItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolItemRepository extends JpaRepository<SchoolItem,Integer> {
    Boolean existsByItemTitle(String itemTitle);
    Optional<SchoolItem> findByItemTitle(String itemTitle);

    //TODO: limit to a specific school
    Page<SchoolItem> findAll(Pageable pageable);
}
