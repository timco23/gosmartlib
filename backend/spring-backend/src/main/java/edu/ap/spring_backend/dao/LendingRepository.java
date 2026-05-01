package edu.ap.spring_backend.dao;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import edu.ap.spring_backend.jpa.Lending;

@Repository
public interface LendingRepository extends ListCrudRepository<Lending, Long> {
    boolean existsByBook_Id(int copyId);
    List<Lending> findByBorrowerEmail(String email);
    List<Lending> findByBookId(Long bookId);
}