package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.TagRepository;
import edu.ap.spring_backend.dto.TagDTO;
import edu.ap.spring_backend.jpa.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    private TagService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TagService(tagRepository);
    }

    @Test
    void create_whenNameExists_throws() {
        when(tagRepository.existsByNameIgnoreCase("Fantasy")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(new TagDTO(null, "Fantasy")));
    }

    @Test
    void create_whenOk_savesTag() {
        when(tagRepository.existsByNameIgnoreCase("Fantasy")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        Tag created = service.create(new TagDTO(null, "Fantasy"));

        assertEquals("Fantasy", created.getName());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void update_whenNotFound_throws() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(1L, new TagDTO(null, "Fantasy")));
    }

    @Test
    void update_whenNameExists_throws() {
        Tag existing = new Tag();
        existing.setName("Old");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tagRepository.existsByNameIgnoreCaseAndIdNot("Fantasy", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.update(1L, new TagDTO(null, "Fantasy")));
    }

    @Test
    void update_whenOk_updatesAndSaves() {
        Tag existing = new Tag();
        existing.setName("Old");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tagRepository.existsByNameIgnoreCaseAndIdNot("Fantasy", 1L)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        Tag updated = service.update(1L, new TagDTO(null, "Fantasy"));

        assertEquals("Fantasy", updated.getName());
        verify(tagRepository).save(existing);
    }

    @Test
    void delete_whenNotFound_throws() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void delete_whenOk_deletes() {
        Tag existing = new Tag();
        when(tagRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(tagRepository).delete(existing);
    }
}