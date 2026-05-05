package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ReadingLevelRepository;
import edu.ap.spring_backend.jpa.ReadingLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReadingLevelServiceTest {

    @Mock
    private ReadingLevelRepository readingLevelRepository;

    private ReadingLevelService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReadingLevelService(readingLevelRepository);
    }

    @Test
    void getAllReadingLevels_returnsRepositoryResult() {
        List<ReadingLevel> levels = List.of(new ReadingLevel());
        when(readingLevelRepository.findAll()).thenReturn(levels);

        List<ReadingLevel> result = service.getAllReadingLevels();

        assertSame(levels, result);
        verify(readingLevelRepository).findAll();
    }
}