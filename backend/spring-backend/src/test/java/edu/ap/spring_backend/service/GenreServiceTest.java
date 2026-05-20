package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.GenreRepository;
import edu.ap.spring_backend.dao.SubgenreRepository;
import edu.ap.spring_backend.dto.GenreWithSubgenresDTO;
import edu.ap.spring_backend.jpa.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class GenreServiceTest {

    private GenreRepository genreRepository;
    private SubgenreRepository subgenreRepository;
    private GenreService genreService;

    @BeforeEach
    void setUp() {
        genreRepository = Mockito.mock(GenreRepository.class);
        subgenreRepository = Mockito.mock(SubgenreRepository.class);
        genreService = new GenreService(genreRepository, subgenreRepository);
    }

    @Test
    void getAll_shouldReturnGenresWithSubgenres() {
        Genre genre = new Genre("Rock");
        genre.setId(1L);

        Mockito.when(genreRepository.findAll()).thenReturn(List.of(genre));
        Mockito.when(subgenreRepository.findAllByGenreId(1L)).thenReturn(List.of());

        List<GenreWithSubgenresDTO> result = genreService.getAll();

        assertEquals(1, result.size());
        assertEquals("Rock", result.get(0).name());
    }

    @Test
    void addGenre_shouldSaveGenre() {
        Genre genre = new Genre("Jazz");

        Mockito.when(genreRepository.save(genre)).thenReturn(genre);

        Genre result = genreService.addGenre(genre);

        assertEquals("Jazz", result.getName());
        Mockito.verify(genreRepository).save(genre);
    }

    @Test
    void updateGenre_shouldUpdateName() {
        Genre existing = new Genre("Old");
        existing.setId(1L);

        Genre updated = new Genre("New");

        Mockito.when(genreRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(genreRepository.save(existing)).thenReturn(existing);

        Genre result = genreService.updateGenre(1L, updated);

        assertEquals("New", result.getName());
    }

    @Test
    void deleteGenre_shouldCallRepository() {
        Genre existing = new Genre("Rock");
        existing.setId(1L);

        Mockito.when(genreRepository.findById(1L)).thenReturn(Optional.of(existing));

        genreService.deleteGenre(1L);

        Mockito.verify(genreRepository).delete(existing);
    }

    @Test
    void getById_shouldReturnGenre() {
        Genre genre = new Genre("Rock");
        genre.setId(1L);

        Mockito.when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

        Genre result = genreService.getById(1L);

        assertEquals("Rock", result.getName());
    }

    @Test
    void getById_shouldThrowIfNotFound() {
        Mockito.when(genreRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> genreService.getById(1L));
    }
}