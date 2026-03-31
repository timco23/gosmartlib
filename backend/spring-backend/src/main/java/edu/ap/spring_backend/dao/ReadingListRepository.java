package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.ReadingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingListRepository extends JpaRepository<ReadingList, Long> {
}
