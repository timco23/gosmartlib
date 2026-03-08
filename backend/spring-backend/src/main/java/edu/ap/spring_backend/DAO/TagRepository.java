package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.Tag;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends ListCrudRepository<Tag, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
