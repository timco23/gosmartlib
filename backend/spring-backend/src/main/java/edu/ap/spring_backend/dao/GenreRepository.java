package edu.ap.spring_backend.dao;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import edu.ap.spring_backend.jpa.Genre;

@Repository
public interface GenreRepository extends ListCrudRepository<Genre, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}