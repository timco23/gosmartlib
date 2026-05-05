package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.SubgenreRepository;
import edu.ap.spring_backend.dto.SubgenreDTO;
import edu.ap.spring_backend.jpa.Genre;
import edu.ap.spring_backend.jpa.Subgenre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SubgenreServiceTest {

    private SubgenreRepository subgenreRepository;
    private GenreService genreService;
    private SubgenreService subgenreService;

    @BeforeEach
    void setUp() {
        subgenreRepository = Mockito.mock(SubgenreRepository.class);
        genreService = Mockito.mock(GenreService.class);
        subgenreService = new SubgenreService(subgenreRepository, genreService);
    }

    @Test
    void getAll_shouldReturnAllSubgenres() {
        Subgenre sub = new Subgenre("Metal");
        sub.setId(1L);

        Mockito.when(subgenreRepository.findAll()).thenReturn(List.of(sub));

        List<Subgenre> result = subgenreService.GetAll();

        assertEquals(1, result.size());
        assertEquals("Metal", result.get(0).getName());
    }

    @Test
    void create_shouldSaveSubgenre_whenNameIsUnique() {
        SubgenreDTO dto = new SubgenreDTO(null, "Metal", 1L);

        Genre genre = new Genre("Rock");
        genre.setId(1L);

        Mockito.when(subgenreRepository.existsByNameIgnoreCase("Metal")).thenReturn(false);
        Mockito.when(genreService.getById(1L)).thenReturn(genre);
        Mockito.when(subgenreRepository.save(Mockito.any(Subgenre.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subgenre result = subgenreService.create(dto);

        assertEquals("Metal", result.getName());
        assertEquals(genre, result.getGenre());

        Mockito.verify(subgenreRepository).save(Mockito.any(Subgenre.class));
    }

    @Test
    void create_shouldThrowException_whenNameAlreadyExists() {
        SubgenreDTO dto = new SubgenreDTO(null, "Metal", 1L);

        Mockito.when(subgenreRepository.existsByNameIgnoreCase("Metal")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> subgenreService.create(dto));

        Mockito.verify(subgenreRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void getFromGenreIds_shouldReturnMatchingSubgenres() {
        Subgenre sub = new Subgenre("Metal");
        sub.setId(1L);

        Mockito.when(subgenreRepository.findAllByGenreIdIn(List.of(1L)))
                .thenReturn(List.of(sub));

        List<Subgenre> result = subgenreService.getFromGenreIds(List.of(1L));

        assertEquals(1, result.size());
    }

    @Test
    void addGenre_shouldSaveSubgenre() {
        Subgenre sub = new Subgenre("Metal");

        Mockito.when(subgenreRepository.save(sub)).thenReturn(sub);

        Subgenre result = subgenreService.addGenre(sub);

        assertEquals("Metal", result.getName());
        Mockito.verify(subgenreRepository).save(sub);
    }

    @Test
    void updateSubgenre_shouldUpdateName() {
        Subgenre existing = new Subgenre("Old");
        existing.setId(1L);

        Subgenre updated = new Subgenre("New");

        Mockito.when(subgenreRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(subgenreRepository.save(existing)).thenReturn(existing);

        Subgenre result = subgenreService.updateSubgenre(1L, updated);

        assertEquals("New", result.getName());
    }

    @Test
    void deleteSubgenre_shouldCallRepository() {
        subgenreService.deleteSubgenre(1L);

        Mockito.verify(subgenreRepository).deleteById(1L);
    }

    @Test
    void updateSubgenre_shouldThrowIfNotFound() {
        Mockito.when(subgenreRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> subgenreService.updateSubgenre(1L, new Subgenre("Test")));
    }
}