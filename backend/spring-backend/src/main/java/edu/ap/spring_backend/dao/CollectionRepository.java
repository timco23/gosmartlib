package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.dto.CollectionDTO;
import edu.ap.spring_backend.jpa.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<CollectionDTO> findBySchoolId(Long schoolId);
    List<CollectionDTO> findBySchoolIdAndFeatured(Long schoolId, Boolean featured);
    List<CollectionDTO> findByGloballyFeatured(Boolean featured);
    Boolean existsByItems_Id(Long itemId);
    Boolean existsByItems_IdAndGloballyFeatured(Long itemId, Boolean featured);
}
