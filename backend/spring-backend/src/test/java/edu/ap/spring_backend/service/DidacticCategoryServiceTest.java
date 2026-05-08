package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.DidacticCategoryRepository;
import edu.ap.spring_backend.dto.DidacticCategoryDTO;
import edu.ap.spring_backend.jpa.DidacticCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DidacticCategoryServiceTest {

    private DidacticCategoryRepository repository;
    private DidacticCategoryService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(DidacticCategoryRepository.class);
        service = new DidacticCategoryService(repository);
    }

    private DidacticCategory buildCategory(Long id, String name) throws Exception {
        DidacticCategory cat = new DidacticCategory(name);
        var idField = DidacticCategory.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(cat, id);
        return cat;
    }

    @Test
    void getAll_shouldReturnAllCategories() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
                buildCategory(1L, "Wiskunde"),
                buildCategory(2L, "Taal")
        ));

        List<DidacticCategory> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals("Wiskunde", result.get(0).getName());
        assertEquals("Taal", result.get(1).getName());
    }

    @Test
    void getAll_empty_shouldReturnEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<DidacticCategory> result = service.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void addDidacticCategory_shouldSaveAndReturnCategory() {
        DidacticCategoryDTO dto = new DidacticCategoryDTO(null, "Wetenschappen");
        when(repository.save(any(DidacticCategory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DidacticCategory result = service.addDidacticCategory(dto);

        assertEquals("Wetenschappen", result.getName());
        verify(repository).save(any(DidacticCategory.class));
    }

    @Test
    void updateDidacticCategory_shouldUpdateNameAndSave() throws Exception {
        DidacticCategory existing = buildCategory(1L, "Oud");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(DidacticCategory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DidacticCategoryDTO dto = new DidacticCategoryDTO(1L, "Nieuw");
        DidacticCategory result = service.updateDidacticCategory(1L, dto);

        assertEquals("Nieuw", result.getName());
        verify(repository).save(existing);
    }

    @Test
    void updateDidacticCategory_notFound_shouldThrow() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        DidacticCategoryDTO dto = new DidacticCategoryDTO(99L, "Onbekend");

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.updateDidacticCategory(99L, dto));

        assertEquals("Entity not found", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteDidacticCategory_shouldDeleteExisting() throws Exception {
        DidacticCategory existing = buildCategory(1L, "Wiskunde");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.deleteDidacticCategory(1L);

        verify(repository).delete(existing);
    }

    @Test
    void deleteDidacticCategory_notFound_shouldThrow() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.deleteDidacticCategory(99L));

        assertEquals("Entity not found", ex.getMessage());
        verify(repository, never()).delete(any());
    }
}