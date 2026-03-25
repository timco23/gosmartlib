package edu.ap.spring_backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ap.spring_backend.jpa.DidacticCategory;

public interface DidacticCategoryRepository extends JpaRepository<DidacticCategory, Long> {
    
}