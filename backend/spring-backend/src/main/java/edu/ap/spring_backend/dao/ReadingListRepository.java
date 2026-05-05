package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.dto.ReadingListDTO;
import edu.ap.spring_backend.jpa.ReadingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingListRepository extends JpaRepository<ReadingList, Long> {
    public List<ReadingListDTO> findByUserId(int userId);
    boolean existsByItems_Id(Long itemId);
}
