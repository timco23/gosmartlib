package edu.ap.spring_backend.dao;

import org.springframework.stereotype.Repository;
import edu.ap.spring_backend.jpa.Subgenre;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

@Repository
public interface SubgenreRepository extends ListCrudRepository<Subgenre, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Subgenre> findAllByGenreIdIn(List<Long> genreIds);

    List<Subgenre> findAllByGenreId(Long genreId);
}