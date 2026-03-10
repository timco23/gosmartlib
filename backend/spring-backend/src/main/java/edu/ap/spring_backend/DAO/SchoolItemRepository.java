package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.SchoolItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolItemRepository extends JpaRepository<SchoolItem,Integer>, JpaSpecificationExecutor<SchoolItem> {
    Boolean existsByItemTitle(String itemTitle);
    Optional<SchoolItem> findByItemTitle(String itemTitle);

}
