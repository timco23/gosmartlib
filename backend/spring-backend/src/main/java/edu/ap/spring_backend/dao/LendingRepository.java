package edu.ap.spring_backend.dao;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import edu.ap.spring_backend.jpa.Lending;

@Repository
public interface LendingRepository extends ListCrudRepository<Lending, Long> {
    
}