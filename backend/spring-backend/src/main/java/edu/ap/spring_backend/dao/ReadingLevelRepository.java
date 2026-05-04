package edu.ap.spring_backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.ap.spring_backend.jpa.ReadingLevel;

@Repository
public interface ReadingLevelRepository extends JpaRepository<ReadingLevel, Long> {
    
}