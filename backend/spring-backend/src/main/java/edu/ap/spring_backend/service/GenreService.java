package edu.ap.spring_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ap.spring_backend.dao.GenreRepository;
import edu.ap.spring_backend.jpa.Genre;

@Service
public class GenreService {
    GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> getAll() {
        return genreRepository.findAll();
    }
}