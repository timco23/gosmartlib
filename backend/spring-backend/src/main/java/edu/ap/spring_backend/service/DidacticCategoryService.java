package edu.ap.spring_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.DidacticCategoryRepository;
import edu.ap.spring_backend.dto.DidacticCategoryDTO;
import edu.ap.spring_backend.jpa.DidacticCategory;

@Service
public class DidacticCategoryService {
    DidacticCategoryRepository didacticCategoryRepository;

    public DidacticCategoryService(DidacticCategoryRepository repository) {
        this.didacticCategoryRepository = repository;
    }

    public List<DidacticCategory> getAll() {
        return didacticCategoryRepository.findAll();
    }

    public DidacticCategory addDidacticCategory(DidacticCategoryDTO didacticCategory){
        DidacticCategory newCategory = new DidacticCategory(didacticCategory.name());
        didacticCategoryRepository.save(newCategory);
        return newCategory;
    }
}