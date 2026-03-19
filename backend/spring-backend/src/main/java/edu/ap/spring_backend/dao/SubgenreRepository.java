package edu.ap.spring_backend.dao;

import org.springframework.stereotype.Repository;
import edu.ap.spring_backend.jpa.Subgenre;
import org.springframework.data.repository.ListCrudRepository;

@Repository
public interface SubgenreRepository extends ListCrudRepository<Subgenre, Long>{
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}