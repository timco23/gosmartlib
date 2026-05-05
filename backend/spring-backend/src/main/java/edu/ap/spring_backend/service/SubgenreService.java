package edu.ap.spring_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.SubgenreRepository;
import edu.ap.spring_backend.dto.SubgenreDTO;
import edu.ap.spring_backend.jpa.Subgenre;

@Service
public class SubgenreService {

    private final SubgenreRepository subgenreRepository;
    private final GenreService genreService;

    public SubgenreService(SubgenreRepository subgenreRepository, GenreService genreService) {
        this.subgenreRepository = subgenreRepository;
        this.genreService = genreService;
    }

    public List<Subgenre> getAll() {
        return subgenreRepository.findAll();
    }

    public Subgenre create(SubgenreDTO newSubgenre) {
        if (subgenreRepository.existsByNameIgnoreCase(newSubgenre.name())) {
            throw new IllegalArgumentException("Tag bestaat reeds");
        }
        Subgenre subgenre = new Subgenre();
        subgenre.setName(newSubgenre.name());
        subgenre.setGenre(genreService.getById(newSubgenre.genreId()));
        return subgenreRepository.save(subgenre);
    }

    public List<SubgenreDTO> getFromGenreIds(List<Long> genreIds) {
        return subgenreRepository.findAllByGenreIdIn(genreIds);
    }

    public Subgenre addGenre(Subgenre newSubgenre) {
        return subgenreRepository.save(newSubgenre);
    }

    public Subgenre updateSubgenre(Long id, Subgenre updatedsubgenre) {
        Subgenre subgenre = subgenreRepository.findById(id).orElseThrow();
        subgenre.setName(updatedsubgenre.getName());
        return subgenreRepository.save(subgenre);
    }

    public void deleteSubgenre(Long id) {
        subgenreRepository.deleteById(id);
    }
}
