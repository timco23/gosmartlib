package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.SchoolItemRepository;
import edu.ap.spring_backend.dao.TagRepository;
import edu.ap.spring_backend.dto.CreateSchoolItemDTO;
import edu.ap.spring_backend.dto.SchoolItemRequestDTO;
import edu.ap.spring_backend.dto.UpdateSchoolItemDTO;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.SchoolItem;
import edu.ap.spring_backend.exception.EntityAlreadyExistsException;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceTest {
        @Mock
        private SchoolItemRepository schoolItemRepository;

        @Mock
        private ItemRepository itemRepository;

        @Mock
        private TagRepository tagRepository;

        @InjectMocks
        private ItemService itemService;

        private Item item;
        private SchoolItem schoolItem;

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);

                item = new Item(
                                "Clean Code",
                                "Prentice Hall",
                                "Robert Martin",
                                "9780132350884",
                                2008,
                                "EN",
                                464,
                                "cover.jpg",
                                4.5,
                                100);
                item.setId(1);

                schoolItem = new SchoolItem(
                                1,
                                new HashSet<>(),
                                "Good book",
                                true,
                                item);
                schoolItem.setId(1);
        }

        // ---------------- GET ALL SCHOOL ITEMS ----------------

        @Test
        void givenSchoolItems_whenGetSchoolItems_thenReturnSchoolDTOList() {
                Page<SchoolItem> page = new PageImpl<>(List.of(schoolItem));

                when(schoolItemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

                SchoolItemRequestDTO request = new SchoolItemRequestDTO(
                                0, 20, "title", "asc", "", List.of(), null, null, null, null, null, List.of());
                var result = itemService.getSchoolItems(request);

                assertEquals(1, result.size());
                assertEquals("Clean Code", result.content().getFirst().title());

                verify(schoolItemRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        void givenNoSchoolItems_whenGetSchoolItems_thenReturnEmptyList() {
                Page<SchoolItem> page = new PageImpl<>(List.of());

                when(schoolItemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

                SchoolItemRequestDTO request = new SchoolItemRequestDTO(
                                0, 20, "title", "asc", "", List.of(), null, null, null, null, null, List.of());
                var result = itemService.getSchoolItems(request);

                assertTrue(result.content().isEmpty());
        }

        @Test
        void givenMinPagesGreaterThanMaxPages_whenGetSchoolItems_thenThrowException() {
                SchoolItemRequestDTO request = new SchoolItemRequestDTO(
                                0, 10, "title", "asc", null, List.of(), 100, 50, null, null, null, List.of());

                assertThrows(IllegalArgumentException.class,
                                () -> itemService.getSchoolItems(request));
        }

        @Test
        void givenMinYearGreaterThanMaxYear_whenGetSchoolItems_thenThrowException() {
                SchoolItemRequestDTO request = new SchoolItemRequestDTO(
                                0, 10, "title", "asc", null, List.of(), null, null, 2020, 2000, null, List.of());

                assertThrows(IllegalArgumentException.class,
                                () -> itemService.getSchoolItems(request));
        }

        @Test
        void givenSearchTerm_whenGetSchoolItems_thenSearchParamIsUsed() {
                Page<SchoolItem> page = new PageImpl<>(List.of(schoolItem));

                when(schoolItemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

                SchoolItemRequestDTO request = new SchoolItemRequestDTO(
                                0, 10, "title", "asc", "Clean Code", List.of(), null, null, null, null, null,
                                List.of());

                var result = itemService.getSchoolItems(request);

                assertEquals(1, result.content().size());
        }

        // ---------------- GET ALL ITEMS ----------------

        @Test
        void givenItems_whenGetAllItems_thenReturnItemDTOList() {

                when(itemRepository.findAll()).thenReturn(List.of(item));

                var result = itemService.getAllItems();

                assertEquals(1, result.size());
                assertEquals("Clean Code", result.get(0).title());

                verify(itemRepository).findAll();
        }

        @Test
        void givenNoItems_whenGetAllItems_thenReturnEmptyList() {

                when(itemRepository.findAll()).thenReturn(List.of());

                var result = itemService.getAllItems();

                assertTrue(result.isEmpty());
        }

        // ---------------- GET SCHOOL ITEM BY ID ----------------

        @Test
        void givenExistingId_whenGetSchoolItemById_thenReturnSchoolItemDTO() {

                when(schoolItemRepository.findById(1)).thenReturn(Optional.of(schoolItem));

                var result = itemService.getSchoolItemById(1);

                assertEquals("Clean Code", result.title());
        }

        @Test
        void givenNonExistingId_whenGetSchoolItemById_thenThrowException() {

                when(schoolItemRepository.findById(1)).thenReturn(Optional.empty());

                assertThrows(EntityNotFoundException.class,
                                () -> itemService.getSchoolItemById(1));
        }

        // ---------------- GET ITEM BY ID ----------------

        @Test
        void givenExistingItem_whenGetItemById_thenReturnItemDTO() {

                when(itemRepository.findById(1)).thenReturn(Optional.of(item));

                var result = itemService.getItemById(1);

                assertEquals("Clean Code", result.title());
        }

        @Test
        void givenMissingItem_whenGetItemById_thenThrowException() {

                when(itemRepository.findById(1)).thenReturn(Optional.empty());

                assertThrows(EntityNotFoundException.class,
                                () -> itemService.getItemById(1));
        }

        // ---------------- GET BY TITLE ----------------

        @Test
        void givenExistingTitle_whenGetSchoolItemByTitle_thenReturnSchoolItemDTO() {

                when(schoolItemRepository.findByItemTitle("Clean Code"))
                                .thenReturn(Optional.of(schoolItem));

                var result = itemService.getSchoolItemByTitle("Clean Code");

                assertEquals("Clean Code", result.title());
        }

        @Test
        void givenMissingTitle_whenGetSchoolItemByTitle_thenThrowException() {

                when(schoolItemRepository.findByItemTitle("Clean Code"))
                                .thenReturn(Optional.empty());

                assertThrows(EntityNotFoundException.class,
                                () -> itemService.getSchoolItemByTitle("Clean Code"));
        }

        // ---------------- ADD ITEM ----------------

        @Test
        void givenCreateSchoolItemDTO_whenAddItem_thenItemIsSaved() {

                CreateSchoolItemDTO dto = new CreateSchoolItemDTO(
                                "Clean Code",
                                "Prentice Hall",
                                "Robert Martin",
                                "9780132350884",
                                2008,
                                "EN",
                                464,
                                "cover.jpg",
                                4.5,
                                100,
                                0,
                                1,
                                List.of(),
                                "Good book",
                                true);

                when(schoolItemRepository.existsByItemTitle("Clean Code"))
                                .thenReturn(false);

                when(itemRepository.existsByTitle("Clean Code"))
                                .thenReturn(false);

                when(itemRepository.save(any(Item.class))).thenReturn(item);
                when(schoolItemRepository.save(any(SchoolItem.class))).thenReturn(schoolItem);

                var result = itemService.addSchoolItem(dto);

                assertEquals("Clean Code", result.title());

                verify(itemRepository).save(any(Item.class));
                verify(schoolItemRepository).save(any(SchoolItem.class));
        }

        @Test
        void givenItemAlreadyExistsInSchool_whenAddItem_thenThrowException() {

                CreateSchoolItemDTO dto = mock(CreateSchoolItemDTO.class);

                when(dto.title()).thenReturn("Clean Code");

                when(schoolItemRepository.existsByItemTitle("Clean Code"))
                                .thenReturn(true);

                assertThrows(EntityAlreadyExistsException.class,
                                () -> itemService.addSchoolItem(dto));
        }

        // ---------------- DELETE ITEM ----------------

        @Test
        void givenExistingSchoolItem_whenDeleteSchoolItem_thenDeleteAndReturnDTO() {

                when(schoolItemRepository.findById(1))
                                .thenReturn(Optional.of(schoolItem));

                var result = itemService.deleteSchoolItem(1);

                assertEquals("Clean Code", result.title());

                verify(schoolItemRepository).delete(schoolItem);
        }

        @Test
        void givenMissingSchoolItem_whenDeleteSchoolItem_thenThrowException() {

                when(schoolItemRepository.findById(1))
                                .thenReturn(Optional.empty());

                assertThrows(EntityNotFoundException.class,
                                () -> itemService.deleteSchoolItem(1));
        }

        // ---------------- UPDATE ITEM ----------------

        @Test
        void givenExistingSchoolItem_whenUpdateSchoolItem_thenItemIsUpdated() {

                UpdateSchoolItemDTO dto = new UpdateSchoolItemDTO(
                                "Clean Code Updated",
                                "Prentice Hall",
                                "Robert Martin",
                                2008,
                                "EN",
                                464,
                                "cover.jpg",
                                List.of(),
                                "Updated description",
                                false);

                when(schoolItemRepository.findById(1))
                                .thenReturn(Optional.of(schoolItem));

                when(itemRepository.save(any(Item.class))).thenReturn(item);
                when(schoolItemRepository.save(any(SchoolItem.class))).thenReturn(schoolItem);

                var result = itemService.updateSchoolItem(1, dto);

                assertEquals("Clean Code Updated", result.title());
        }

        @Test
        void givenMissingSchoolItem_whenUpdateSchoolItem_thenThrowException() {

                UpdateSchoolItemDTO dto = mock(UpdateSchoolItemDTO.class);

                when(schoolItemRepository.findById(1))
                                .thenReturn(Optional.empty());

                assertThrows(EntityNotFoundException.class,
                                () -> itemService.updateSchoolItem(1, dto));
        }

}