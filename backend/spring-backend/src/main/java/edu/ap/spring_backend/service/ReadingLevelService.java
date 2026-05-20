package edu.ap.spring_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.ReadingLevelRepository;
import edu.ap.spring_backend.jpa.ReadingLevel;

@Service
public class ReadingLevelService {
    private final ReadingLevelRepository readingLevelRepository;

    public ReadingLevelService(ReadingLevelRepository repository){
        readingLevelRepository = repository;
    }

    public List<ReadingLevel> getAllReadingLevels(){
        return readingLevelRepository.findAll();
    }
}
