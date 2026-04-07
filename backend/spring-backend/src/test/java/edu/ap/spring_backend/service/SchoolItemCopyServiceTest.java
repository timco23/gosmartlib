package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.CampusRepository;
import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.SchoolItemCopyDTO;
import edu.ap.spring_backend.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchoolItemCopyServiceTest {

    @Mock private SchoolItemCopyRepository schoolItemCopyRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private CampusRepository campusRepository;

    @InjectMocks
    private SchoolItemCopyService service;

    private School testSchool;
    private Item testItem;
    private SchoolItemCopy copy1;
    private SchoolItemCopy copy2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testSchool = new School();
        testSchool.setId(1L);

        testItem = new Item();
        testItem.setId(1L);

        copy1 = new SchoolItemCopy();
        copy1.setId(1);
        copy1.setBarcode("BC000001");
        copy1.setCondition(ItemCondition.GOED);
        copy1.setCopy_status(CopyStatus.BESCHIKBAAR);
        copy1.setFeatured(false);
        copy1.setItem(testItem);
        copy1.setSchool(testSchool);

        copy2 = new SchoolItemCopy();
        copy2.setId(2);
        copy2.setBarcode("BC000002");
        copy2.setCondition(ItemCondition.NIEUW);
        copy2.setCopy_status(CopyStatus.BESCHIKBAAR);
        copy2.setFeatured(false);
        copy2.setItem(testItem);
        copy2.setSchool(testSchool);
    }

    // ---------------- getCopiesByIds ----------------

    @Test
    void givenExistingIds_whenGetCopiesByIds_thenReturnMatchingDTOs() {
        // Given
        when(schoolItemCopyRepository.findAllById(List.of(1, 2))).thenReturn(List.of(copy1, copy2));

        // When
        List<SchoolItemCopyDTO> result = service.getCopiesByIds(List.of(1, 2));

        // Then
        assertEquals(2, result.size());
        assertEquals("BC000001", result.get(0).barcode());
        assertEquals("BC000002", result.get(1).barcode());
        verify(schoolItemCopyRepository).findAllById(List.of(1, 2));
    }

    @Test
    void givenSingleId_whenGetCopiesByIds_thenReturnSingleDTO() {
        // Given
        when(schoolItemCopyRepository.findAllById(List.of(1))).thenReturn(List.of(copy1));

        // When
        List<SchoolItemCopyDTO> result = service.getCopiesByIds(List.of(1));

        // Then
        assertEquals(1, result.size());
        assertEquals("BC000001", result.get(0).barcode());
    }

    @Test
    void givenNoMatchingIds_whenGetCopiesByIds_thenReturnEmptyList() {
        // Given
        when(schoolItemCopyRepository.findAllById(List.of(99, 100))).thenReturn(List.of());

        // When
        List<SchoolItemCopyDTO> result = service.getCopiesByIds(List.of(99, 100));

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void givenPartialMatch_whenGetCopiesByIds_thenReturnOnlyFoundCopies() {
        // Given — repo returns only copy1 even though 2 IDs were requested (copy 99 doesn't exist)
        when(schoolItemCopyRepository.findAllById(List.of(1, 99))).thenReturn(List.of(copy1));

        // When
        List<SchoolItemCopyDTO> result = service.getCopiesByIds(List.of(1, 99));

        // Then
        assertEquals(1, result.size());
        assertEquals("BC000001", result.get(0).barcode());
    }
}
