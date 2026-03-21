package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.*;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.exception.EntityAlreadyExistsException;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private SchoolRepository schoolRepository;
    @Mock
    private CampusRepository campusRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private SchoolItemCopyRepository schoolItemCopyRepository;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;
    private School testSchool;
    private Campus testCampus;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Science Fiction");

        testItem = new Item(
                "Test Description",
                10,
                4.5,
                "test-cover.jpg",
                300,
                "English",
                2021,
                "1234567890",
                "Test Author",
                "Test Publisher",
                "Test Title",
                new HashSet<>(Collections.singletonList(testTag))
        );
        testItem.setId(1);

        testSchool = new School();
        testSchool.setId(1L);
        testSchool.setName("Test School");

        testCampus = new Campus();
        testCampus.setId(1L);
        testCampus.setName("Test Campus");
        testCampus.setSchool(testSchool);
    }

    // ---------------- getSchoolItems ----------------

    @Test
    void givenValidRequest_whenGetSchoolItems_thenReturnPageResponse() {
        // Given
        ItemRequestDTO request = new ItemRequestDTO(
                0, 10, "title", "asc", "Test", List.of("English"),
                100, 500, 2000, 2025, 4.0, List.of(1L)
        );
        Page<Item> page = new PageImpl<>(List.of(testItem));
        when(itemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // When
        PageResponseDTO<ItemDTO> response = itemService.getFilteredItems(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals("Test Title", response.content().get(0).title());
        verify(itemRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void givenInvalidPages_whenGetSchoolItems_thenThrowIllegalArgumentException() {
        // Given
        ItemRequestDTO request = new ItemRequestDTO(
                0, 10, "title", "asc", null, null,
                500, 100, null, null, null, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> itemService.getFilteredItems(request));
    }

    @Test
    void givenInvalidYears_whenGetSchoolItems_thenThrowIllegalArgumentException() {
        // Given
        ItemRequestDTO request = new ItemRequestDTO(
                0, 10, "title", "asc", null, null,
                null, null, 2025, 2020, null, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> itemService.getFilteredItems(request));
    }

    // ---------------- addItem ----------------

    @Test
    void givenNewItem_whenAddItem_thenCreateItemAndCopy() {
        // Given
        CreateItemDTO dto = new CreateItemDTO(
                "Test Title", "Publisher", "Author", "ISBN", 2021, "English", 300, "cover.jpg",
                4.5, 10, 0, 1L, List.of(1L), "Description", true, 1L, "BARCODE123",
                CopyStatus.BESCHIKBAAR, ItemCondition.GOED
        );

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId(dto.title(), dto.schoolId())).thenReturn(false);
        when(itemRepository.existsByTitle(dto.title())).thenReturn(false);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(campusRepository.findById(1L)).thenReturn(Optional.of(testCampus));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        // When
        ItemDTO result = itemService.addItem(dto);

        // Then
        assertNotNull(result);
        assertEquals("Test Title", result.title());
        verify(itemRepository).save(any(Item.class));
        verify(schoolItemCopyRepository).save(any(SchoolItemCopy.class));
    }

    @Test
    void givenExistingItemById_whenAddItem_thenCreateOnlyCopy() {
        // Given
        CreateItemDTO dto = new CreateItemDTO(
                "Test Title", "Publisher", "Author", "ISBN", 2021, "English", 300, "cover.jpg",
                4.5, 10, 1, 1L, List.of(1L), "Description", true, 1L, "BARCODE123",
                CopyStatus.BESCHIKBAAR, ItemCondition.GOED
        );

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId(dto.title(), dto.schoolId())).thenReturn(false);
        when(itemRepository.existsByTitle(dto.title())).thenReturn(true);
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(campusRepository.findById(1L)).thenReturn(Optional.of(testCampus));

        // When
        ItemDTO result = itemService.addItem(dto);

        // Then
        assertNotNull(result);
        verify(itemRepository, never()).save(any(Item.class));
        verify(schoolItemCopyRepository).save(any(SchoolItemCopy.class));
    }

    @Test
    void givenExistingItemByTitle_whenAddItem_thenCreateOnlyCopy() {
        // Given
        CreateItemDTO dto = new CreateItemDTO(
                "Test Title", "Publisher", "Author", "ISBN", 2021, "English", 300, "cover.jpg",
                4.5, 10, 0, 1L, List.of(1L), "Description", true, 1L, "BARCODE123",
                CopyStatus.BESCHIKBAAR, ItemCondition.GOED
        );

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId(dto.title(), dto.schoolId())).thenReturn(false);
        when(itemRepository.existsByTitle(dto.title())).thenReturn(true);
        when(itemRepository.findByTitle(dto.title())).thenReturn(Optional.of(testItem));
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(campusRepository.findById(1L)).thenReturn(Optional.of(testCampus));

        // When
        ItemDTO result = itemService.addItem(dto);

        // Then
        assertNotNull(result);
        verify(itemRepository, never()).save(any(Item.class));
        verify(schoolItemCopyRepository).save(any(SchoolItemCopy.class));
    }

    @Test
    void givenExistingCopy_whenAddItem_thenThrowEntityAlreadyExistsException() {
        // Given
        CreateItemDTO dto = new CreateItemDTO(
                "Test Title", "Publisher", "Author", "ISBN", 2021, "English", 300, "cover.jpg",
                4.5, 10, 0, 1L, List.of(1L), "Description", true, 1L, "BARCODE123",
                CopyStatus.BESCHIKBAAR, ItemCondition.GOED
        );

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId(dto.title(), dto.schoolId())).thenReturn(true);

        // When & Then
        assertThrows(EntityAlreadyExistsException.class, () -> itemService.addItem(dto));
    }

    @Test
    void givenNonExistingSchool_whenAddItem_thenThrowEntityNotFoundException() {
        // Given
        CreateItemDTO dto = new CreateItemDTO(
                "Test Title", "Publisher", "Author", "ISBN", 2021, "English", 300, "cover.jpg",
                4.5, 10, 1, 1L, List.of(1L), "Description", true, 1L, "BARCODE123",
                CopyStatus.BESCHIKBAAR, ItemCondition.GOED
        );

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId(dto.title(), dto.schoolId())).thenReturn(false);
        when(itemRepository.existsByTitle(dto.title())).thenReturn(true);
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> itemService.addItem(dto));
    }

    // ---------------- deleteItem ----------------

    @Test
    void givenExistingId_whenDeleteItem_thenDeleteAndReturnDTO() {
        // Given
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));

        // When
        ItemDTO result = itemService.deleteItem(1);

        // Then
        assertNotNull(result);
        assertEquals(testItem.getTitle(), result.title());
        verify(itemRepository).delete(testItem);
    }

    @Test
    void givenNonExistingId_whenDeleteItem_thenThrowEntityNotFoundException() {
        // Given
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> itemService.deleteItem(1));
    }

    // ---------------- getAllItems ----------------

    @Test
    void givenItemsExist_whenGetAllItems_thenReturnList() {
        // Given
        when(itemRepository.findAll()).thenReturn(List.of(testItem));

        // When
        List<ItemDTO> result = itemService.getAllItems();

        // Then
        assertEquals(1, result.size());
        assertEquals(testItem.getTitle(), result.get(0).title());
    }

    // ---------------- updateSchoolItem ----------------

    @Test
    void givenExistingId_whenUpdateSchoolItem_thenUpdateAndReturnDTO() {
        // Given
        UpdateItemDTO dto = new UpdateItemDTO(
                "Updated Title", "Updated Publisher", "Updated Author", 2022, "French", 350,
                "updated-cover.jpg", List.of(1L), "Updated Description", "0987654321"
        );
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));

        // When
        ItemDTO result = itemService.updateItem(1, dto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", result.title());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void givenNonExistingId_whenUpdateSchoolItem_thenThrowEntityNotFoundException() {
        // Given
        UpdateItemDTO dto = new UpdateItemDTO(
                "Updated Title", "Updated Publisher", "Updated Author", 2022, "French", 350,
                "updated-cover.jpg", List.of(1L), "Updated Description", "0987654321"
        );
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(1, dto));
    }

    // ---------------- getItemById ----------------

    @Test
    void givenExistingId_whenGetItemById_thenReturnDTO() {
        // Given
        when(itemRepository.findById(1)).thenReturn(Optional.of(testItem));

        // When
        ItemDTO result = itemService.getItemById(1);

        // Then
        assertNotNull(result);
        assertEquals(testItem.getTitle(), result.title());
    }

    @Test
    void givenNonExistingId_whenGetItemById_thenThrowEntityNotFoundException() {
        // Given
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> itemService.getItemById(1));
    }

    // ---------------- getItemByTitle ----------------

    @Test
    void givenExistingTitle_whenGetItemByTitle_thenReturnDTO() {
        // Given
        when(itemRepository.findByTitle("Test Title")).thenReturn(Optional.of(testItem));

        // When
        ItemDTO result = itemService.getItemByTitle("Test Title");

        // Then
        assertNotNull(result);
        assertEquals("Test Title", result.title());
    }

    @Test
    void givenNonExistingTitle_whenGetItemByTitle_thenReturnNull() {
        // Given
        when(itemRepository.findByTitle("Non Existing")).thenReturn(Optional.empty());

        // When
        ItemDTO result = itemService.getItemByTitle("Non Existing");

        // Then
        assertNull(result);
    }
}
