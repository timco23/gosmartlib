package edu.ap.spring_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.SubgenreRepository;
import edu.ap.spring_backend.dto.SubgenreDTO;
import edu.ap.spring_backend.jpa.Subgenre;

@Service
public class SubgenreService {

    private final SubgenreRepository subgenreRepository;

    public SubgenreService(SubgenreRepository subgenreRepository){
        this.subgenreRepository = subgenreRepository;
    }
    
    public List<Subgenre> GetAll(){
        return subgenreRepository.findAll();
    }

    public Subgenre create(SubgenreDTO newSubgenre) {
        if (subgenreRepository.existsByNameIgnoreCase(newSubgenre.name())) {
            throw new IllegalArgumentException("Tag bestaat reeds");
        }
        Subgenre subgenre = new Subgenre();
        subgenre.setName(newSubgenre.name());
        return subgenreRepository.save(subgenre);
    }

    public List<Subgenre> getFromGenreId(long genreId) {
        return subgenreRepository.findAllByGenreId(genreId);
    }
}
