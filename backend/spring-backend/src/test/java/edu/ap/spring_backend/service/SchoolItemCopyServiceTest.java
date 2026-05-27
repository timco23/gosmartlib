package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.CampusRepository;
import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dto.SchoolItemCopyDTO;
import edu.ap.spring_backend.dto.SchoolItemCopyExcelDTO;
import edu.ap.spring_backend.dto.SchoolItemCopyExcelRowDTO;
import edu.ap.spring_backend.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Test
    void givenSchoolItemCopyExcelDTO_whenGenerateExcel_thenReturnExcelWithCorrectData() throws IOException {
        // Given
        SchoolItemCopyExcelRowDTO row1 = new SchoolItemCopyExcelRowDTO("testBook1", "0001");
        SchoolItemCopyExcelRowDTO row2 = new SchoolItemCopyExcelRowDTO("testBook2", "0002");
        SchoolItemCopyExcelDTO excelDTO = new SchoolItemCopyExcelDTO(new SchoolItemCopyExcelRowDTO[]{row1, row2});

        // When
        Path excelLocation = service.generateExcel(excelDTO);

        // Then
        assertNotNull(excelLocation);
        assertTrue(Files.exists(excelLocation));

        try (Workbook workbook = WorkbookFactory.create(excelLocation.toFile())) {
            Sheet sheet = workbook.getSheet("copies");
            assertNotNull(sheet);

            // Header row
            Row headerRow = sheet.getRow(0);
            assertEquals("bookName", headerRow.getCell(0).getStringCellValue());
            assertEquals("copyId", headerRow.getCell(1).getStringCellValue());

            // Data row 1
            Row dataRow1 = sheet.getRow(1);
            assertEquals("testBook1", dataRow1.getCell(0).getStringCellValue());
            assertEquals("0001", dataRow1.getCell(1).getStringCellValue());

            // Data row 2
            Row dataRow2 = sheet.getRow(2);
            assertEquals("testBook2", dataRow2.getCell(0).getStringCellValue());
            assertEquals("0002", dataRow2.getCell(1).getStringCellValue());
        } finally {
            Files.deleteIfExists(excelLocation);
        }
    }
}
