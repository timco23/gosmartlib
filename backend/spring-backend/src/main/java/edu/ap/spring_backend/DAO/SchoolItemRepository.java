package edu.ap.spring_backend.DAO;

import edu.ap.spring_backend.entity.SchoolItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolItemRepository extends JpaRepository<SchoolItem,Integer> {
    Boolean existsByItemTitle(String itemTitle);
}
