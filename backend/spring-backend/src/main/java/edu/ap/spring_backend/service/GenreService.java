package edu.ap.spring_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.GenreRepository;
import edu.ap.spring_backend.dao.SubgenreRepository;
import edu.ap.spring_backend.dto.GenreWithSubgenresDTO;
import edu.ap.spring_backend.dto.SubgenreDTO;
import edu.ap.spring_backend.jpa.Genre;

@Service
public class GenreService {
    private final GenreRepository genreRepository;
    private final SubgenreRepository subgenreRepository;

    public GenreService(GenreRepository genreRepository, SubgenreRepository subgenreRepository) {
        this.genreRepository = genreRepository;
        this.subgenreRepository = subgenreRepository;
    }

    public List<GenreWithSubgenresDTO> getAll() {
        return genreRepository.findAll().stream()
                .map(genre -> new GenreWithSubgenresDTO(
                        genre.getId(),
                        genre.getName(),
                        subgenreRepository.findAllByGenreId(genre.getId())
                                .stream()
                                .map(s -> new SubgenreDTO(s.getId(), s.getName()))
                                .toList()))
                .toList();
    }

    public Genre addGenre(Genre newGenre) {
        return genreRepository.save(newGenre);
    }

    public Genre updateGenre(Long id, Genre updatedGenre) {
        Genre genre = genreRepository.findById(id).orElseThrow();
        genre.setName(updatedGenre.getName());
        return genreRepository.save(genre);
    }

    public void deleteGenre(Long id) {
        genreRepository.deleteById(id);
    }
}