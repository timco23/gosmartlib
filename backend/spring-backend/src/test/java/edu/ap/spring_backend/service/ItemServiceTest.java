package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.*;
import edu.ap.spring_backend.dto.*;
import edu.ap.spring_backend.exception.EntityAlreadyExistsException;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private SchoolRepository schoolRepository;
    @Mock private CampusRepository campusRepository;
    @Mock private TagRepository tagRepository;
    @Mock private SchoolItemCopyRepository schoolItemCopyRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private SubgenreRepository subgenreRepository;
    @Mock private ReadingLevelRepository readingLevelRepository;
    @Mock private DidacticCategoryRepository didacticCategoryRepository;

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

        testSchool = new School();
        testSchool.setId(1L);
        testSchool.setName("Test School");

        testCampus = new Campus();
        testCampus.setId(1L);

        testItem = new Item();
        testItem.setId(1L);
        testItem.setTitle("Dune");
        testItem.setAuthor("Frank Herbert");
        testItem.setPublisher("Ace");
        testItem.setIsbn("9780441568529");
        testItem.setBookLanguage("EN");
        testItem.setPageCount(412);
        testItem.setTags(new HashSet<>(Set.of(testTag)));
    }

    // ---------------- getFilteredItems ----------------

    @Test
    void givenValidRequest_whenGetFilteredItems_thenReturnPageResponse() {
        ItemRequestDTO request = buildRecord(ItemRequestDTO.class, Map.of(
                "page", 0,
                "size", 10,
                "sortBy", "author",
                "sortDirection", "desc",
                "schoolSpecificBooks", false
        ));

        Page<Item> page = new PageImpl<>(List.of(testItem));
        when(itemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponseDTO<ItemDTO> response = itemService.getFilteredItems(request);

        assertNotNull(response);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(itemRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("author").getDirection());
    }

    @Test
    void givenInvalidPages_whenGetFilteredItems_thenThrowIllegalArgumentException() {
        ItemRequestDTO request = buildRecord(ItemRequestDTO.class, Map.of(
                "minPages", 200,
                "maxPages", 100,
                "page", 0,
                "size", 10
        ));

        assertThrows(IllegalArgumentException.class, () -> itemService.getFilteredItems(request));
    }

    // ---------------- addItem ----------------

    @Test
    void givenNewItem_whenAddItem_thenCreateItemAndCopy() {
        CreateItemDTO dto = createItemDto(Map.of());

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId("Dune", 1L)).thenReturn(false);
        when(itemRepository.existsByTitle("Dune")).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));

        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(10L);
            return i;
        });
        when(schoolItemCopyRepository.save(any(SchoolItemCopy.class))).thenAnswer(inv -> {
            SchoolItemCopy c = inv.getArgument(0);
            if (c.getId() == 0) c.setId(1);
            return c;
        });

        ItemDTO result = itemService.addItem(dto);

        assertNotNull(result);
        assertEquals("Dune", result.title());

        ArgumentCaptor<SchoolItemCopy> copyCaptor = ArgumentCaptor.forClass(SchoolItemCopy.class);
        verify(schoolItemCopyRepository, times(2)).save(copyCaptor.capture());
        assertTrue(copyCaptor.getAllValues().stream().anyMatch(c -> "BC000001".equals(c.getBarcode())));
    }

    @Test
    void givenExistingItemById_whenAddItem_thenCreateOnlyCopy() {
        CreateItemDTO dto = createItemDto(Map.of("itemId", 5L));

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId("Dune", 1L)).thenReturn(false);
        when(itemRepository.existsByTitle("Dune")).thenReturn(true);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(testItem));
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(schoolItemCopyRepository.save(any(SchoolItemCopy.class))).thenAnswer(inv -> {
            SchoolItemCopy c = inv.getArgument(0);
            if (c.getId() == 0) c.setId(1);
            return c;
        });

        itemService.addItem(dto);

        verify(itemRepository, never()).save(any(Item.class));
        verify(schoolItemCopyRepository, times(2)).save(any(SchoolItemCopy.class));
    }

    @Test
    void givenExistingItemByTitle_whenAddItem_thenCreateOnlyCopy() {
        CreateItemDTO dto = createItemDto(Map.of("itemId", 0L));

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId("Dune", 1L)).thenReturn(false);
        when(itemRepository.existsByTitle("Dune")).thenReturn(true);
        when(itemRepository.findByTitle("Dune")).thenReturn(Optional.of(testItem));
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(schoolItemCopyRepository.save(any(SchoolItemCopy.class))).thenAnswer(inv -> {
            SchoolItemCopy c = inv.getArgument(0);
            if (c.getId() == 0) c.setId(1);
            return c;
        });

        itemService.addItem(dto);

        verify(itemRepository, never()).save(any(Item.class));
        verify(schoolItemCopyRepository, times(2)).save(any(SchoolItemCopy.class));
    }

    @Test
    void givenExistingCopy_whenAddItem_thenThrowEntityAlreadyExistsException() {
        CreateItemDTO dto = createItemDto(Map.of());

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId("Dune", 1L)).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () -> itemService.addItem(dto));
    }

    @Test
    void givenNonExistingSchool_whenAddItem_thenThrowEntityNotFoundException() {
        CreateItemDTO dto = createItemDto(Map.of());

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId("Dune", 1L)).thenReturn(false);
        when(itemRepository.existsByTitle("Dune")).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.addItem(dto));
    }

    @Test
    void givenIsDidacticTrue_whenAddItem_thenIsDidacticSaved() {
        CreateItemDTO dto = createItemDto(Map.of("isDidactic", true));

        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId("Dune", 1L)).thenReturn(false);
        when(itemRepository.existsByTitle("Dune")).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        when(schoolItemCopyRepository.save(any(SchoolItemCopy.class))).thenAnswer(inv -> {
            SchoolItemCopy c = inv.getArgument(0);
            if (c.getId() == 0) c.setId(1);
            return c;
        });

        ItemDTO result = itemService.addItem(dto);

        assertTrue(readDidactic(result));
    }

    @Test
    void givenDidacticCategoriesNonEmpty_whenAddItem_thenIsDidacticForcedTrue() {
        DidacticCategory cat = new DidacticCategory();
        cat.setId(1L);

        CreateItemDTO dto = createItemDto(Map.of("didacticCategories", List.of(1L)));

        when(didacticCategoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(schoolItemCopyRepository.existsByItemTitleAndSchoolId("Dune", 1L)).thenReturn(false);
        when(itemRepository.existsByTitle("Dune")).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        when(schoolItemCopyRepository.save(any(SchoolItemCopy.class))).thenAnswer(inv -> {
            SchoolItemCopy c = inv.getArgument(0);
            if (c.getId() == 0) c.setId(1);
            return c;
        });

        ItemDTO result = itemService.addItem(dto);

        assertTrue(readDidactic(result));
    }

    // ---------------- deleteItem ----------------

    @Test
    void givenExistingId_whenDeleteItem_thenDeleteAndReturnDTO() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        ItemDTO result = itemService.deleteItem(1L);

        assertEquals("Dune", result.title());
        verify(itemRepository).delete(testItem);
    }

    @Test
    void givenNonExistingId_whenDeleteItem_thenThrowEntityNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.deleteItem(1L));
    }

    // ---------------- getAllItems ----------------

    @Test
    void givenItemsExist_whenGetAllItems_thenReturnList() {
        when(itemRepository.findAll()).thenReturn(List.of(testItem));

        List<ItemDTO> result = itemService.getAllItems();

        assertEquals(1, result.size());
        assertEquals("Dune", result.get(0).title());
    }

    // ---------------- updateItem ----------------

    @Test
    void givenExistingId_whenUpdateItem_thenUpdateAndReturnDTO() {
        UpdateItemDTO dto = createUpdateItemDto(Map.of("title", "Updated"));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDTO result = itemService.updateItem(1L, dto);

        assertEquals("Updated", result.title());
        verify(itemRepository).save(testItem);
    }

    @Test
    void givenIsDidacticTrue_whenUpdateItem_thenIsDidacticSaved() {
        UpdateItemDTO dto = createUpdateItemDto(Map.of("isDidactic", true));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDTO result = itemService.updateItem(1L, dto);

        assertTrue(readDidactic(result));
    }

    @Test
    void givenDidacticCategoriesNonEmpty_whenUpdateItem_thenIsDidacticForcedTrue() {
        DidacticCategory cat = new DidacticCategory();
        cat.setId(1L);

        UpdateItemDTO dto = createUpdateItemDto(Map.of("didacticCategories", List.of(1L)));

        when(didacticCategoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDTO result = itemService.updateItem(1L, dto);

        assertTrue(readDidactic(result));
    }

    @Test
    void givenNonExistingId_whenUpdateItem_thenThrowEntityNotFoundException() {
        UpdateItemDTO dto = createUpdateItemDto(Map.of());

        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(1L, dto));
    }

    // ---------------- getItemById ----------------

    @Test
    void givenExistingId_whenGetItemById_thenReturnDTO() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        ItemDTO result = itemService.getItemById(1L);

        assertEquals("Dune", result.title());
    }

    @Test
    void givenNonExistingId_whenGetItemById_thenThrowEntityNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.getItemById(1L));
    }

    @Test
    void givenExistingTitle_whenGetItemByTitle_thenReturnDTO() {
        when(itemRepository.findByTitle("Dune")).thenReturn(Optional.of(testItem));

        ItemDTO result = itemService.getItemByTitle("Dune");

        assertNotNull(result);
        assertEquals("Dune", result.title());
    }

    @Test
    void givenNonExistingTitle_whenGetItemByTitle_thenReturnNull() {
        when(itemRepository.findByTitle("Unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.getItemByTitle("Unknown"));
    }

    @Test
    void givenSchoolId_whenGetFeaturedItems_thenReturnList() {
        when(itemRepository.findAll(any(Specification.class))).thenReturn(List.of(testItem));

        List<ItemDTO> result = itemService.getFeaturedItems(1L);

        assertEquals(1, result.size());
        assertEquals("Dune", result.get(0).title());
    }

    @Test
    void givenCopies_whenGet10LatestItems_thenReturnUniqueItems() {
        SchoolItemCopy c1 = new SchoolItemCopy();
        c1.setItem(testItem);

        Item other = new Item();
        other.setId(2L);
        other.setTitle("Neuromancer");

        SchoolItemCopy c2 = new SchoolItemCopy();
        c2.setItem(testItem); // duplicate item

        SchoolItemCopy c3 = new SchoolItemCopy();
        c3.setItem(other);

        when(schoolItemCopyRepository.findTop50BySchoolIdOrderByIdDesc(1L))
                .thenReturn(List.of(c1, c2, c3));

        List<ItemDTO> result = itemService.get10LatestItems(1L);

        assertEquals(2, result.size());
        assertEquals(Set.of("Dune", "Neuromancer"),
                result.stream().map(ItemDTO::title).collect(Collectors.toSet()));
    }

    private CreateItemDTO createItemDto(Map<String, Object> overrides) {
        Map<String, Object> values = new HashMap<>();
        values.put("title", "Dune");
        values.put("author", "Frank Herbert");
        values.put("publisher", "Ace");
        values.put("isbn", "9780441568529");
        values.put("releaseYear", 1965);
        values.put("bookLanguage", "EN");
        values.put("pageCount", 412);
        values.put("cover", "cover.jpg");
        values.put("reviewScore", 0.0);
        values.put("reviewCount", 0);
        values.put("description", "desc");
        values.put("tags", List.of());
        values.put("genres", List.of());
        values.put("subgenres", List.of());
        values.put("readingLevel", null);
        values.put("didacticCategories", List.of());
        values.put("schoolId", 1L);
        values.put("itemId", 0L);
        values.put("featured", false);
        values.put("campusId", null);
        values.put("copyStatus", CopyStatus.BESCHIKBAAR);
        values.put("condition", ItemCondition.GOED);
        values.put("isDidactic", false);
        values.putAll(overrides);
        return buildRecord(CreateItemDTO.class, values);
    }

    private UpdateItemDTO createUpdateItemDto(Map<String, Object> overrides) {
        Map<String, Object> values = new HashMap<>();
        values.put("title", "Updated");
        values.put("author", "Author");
        values.put("publisher", "Publisher");
        values.put("isbn", "1111111111111");
        values.put("releaseYear", 2000);
        values.put("bookLanguage", "EN");
        values.put("pageCount", 100);
        values.put("cover", "new.jpg");
        values.put("description", "desc");
        values.put("tags", List.of());
        values.put("genres", List.of());
        values.put("subgenres", List.of());
        values.put("readingLevel", null);
        values.put("didacticCategories", List.of());
        values.put("isDidactic", false);
        values.putAll(overrides);
        return buildRecord(UpdateItemDTO.class, values);
    }

    private static boolean readDidactic(ItemDTO dto) {
        try {
            return (boolean) dto.getClass().getMethod("isDidactic").invoke(dto);
        } catch (Exception ex) {
            try {
                return (boolean) dto.getClass().getMethod("didactic").invoke(dto);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static <T> T buildRecord(Class<T> recordClass, Map<String, Object> values) {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException("Not a record: " + recordClass);
        }
        try {
            RecordComponent[] components = recordClass.getRecordComponents();
            Object[] args = new Object[components.length];
            Class<?>[] types = new Class<?>[components.length];

            for (int i = 0; i < components.length; i++) {
                RecordComponent c = components[i];
                types[i] = c.getType();
                Object value = values.get(c.getName());
                args[i] = coerceValue(types[i], value);
            }

            Constructor<T> ctor = recordClass.getDeclaredConstructor(types);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Object coerceValue(Class<?> type, Object value) {
        if (value == null) return defaultValue(type);
        if (type.isInstance(value)) return value;

        if (type.isPrimitive()) {
            if (type == int.class) return ((Number) value).intValue();
            if (type == long.class) return ((Number) value).longValue();
            if (type == double.class) return ((Number) value).doubleValue();
            if (type == boolean.class) return (value instanceof Boolean) ? value : Boolean.parseBoolean(value.toString());
        }

        if (Number.class.isAssignableFrom(type) && value instanceof Number n) {
            if (type == Integer.class) return n.intValue();
            if (type == Long.class) return n.longValue();
            if (type == Double.class) return n.doubleValue();
        }

        return value;
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0d;
        if (type == float.class) return 0f;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        return null;
    }
}