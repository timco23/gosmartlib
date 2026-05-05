package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dao.ReadingListRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.CreateReadingListDTO;
import edu.ap.spring_backend.dto.ReadingListDTO;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.ReadingList;
import edu.ap.spring_backend.jpa.Tag;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReadingListServiceTest {

    @Mock private ReadingListRepository readingListRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReadingListService service;

    private User user;
    private Item item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = mock(User.class);
        when(user.getId()).thenReturn(1);
        when(user.getFirstName()).thenReturn("Jane");
        when(user.getName()).thenReturn("Doe");

        item = new Item();
        item.setId(1L);
        item.setTitle("Dune");
        item.setAuthor("Frank Herbert");

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("SciFi");

        item.setTags(new HashSet<>(Set.of(tag)));
        item.setGenres(new HashSet<>());
        item.setSubgenres(new HashSet<>());
        item.setDidacticCategories(new HashSet<>());
    }

    @Test
    void findAll_returnsDtos() {
        ReadingList list = new ReadingList();
        list.setId(1L);
        list.setTitle("List");
        list.setDescription("Desc");
        list.setUser(user);
        list.setItems(List.of(item));

        when(readingListRepository.findAll()).thenReturn(List.of(list));

        List<ReadingListDTO> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("List", result.get(0).title());
    }

    @Test
    void findById_existing_returnsDto() {
        ReadingList list = new ReadingList();
        list.setId(1L);
        list.setTitle("List");
        list.setDescription("Desc");
        list.setUser(user);
        list.setItems(List.of(item));

        when(readingListRepository.findById(1L)).thenReturn(Optional.of(list));

        ReadingListDTO result = service.findById(1L);

        assertEquals("List", result.title());
    }

    @Test
    void findById_missing_throws() {
        when(readingListRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void findByUserId_returnsDtos() {
        ReadingList list = new ReadingList();
        list.setId(1L);
        list.setTitle("List");
        list.setDescription("Desc");
        list.setUser(user);
        list.setItems(List.of(item));

        when(readingListRepository.findByUserId(1)).thenReturn(List.of(list));

        List<ReadingListDTO> result = service.findByUserId(1);

        assertEquals(1, result.size());
    }

    @Test
    void addReadingList_withItems_savesAndReturnsDto() {
        CreateReadingListDTO dto = buildRecord(CreateReadingListDTO.class, Map.of(
                "title", "List",
                "description", "Desc",
                "userId", 1,
                "itemIds", List.of(1L)
        ));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(readingListRepository.save(any(ReadingList.class))).thenAnswer(inv -> inv.getArgument(0));

        ReadingListDTO result = service.addReadingList(dto);

        assertEquals("List", result.title());
        verify(readingListRepository).save(any(ReadingList.class));
    }

    @Test
    void addReadingList_withoutItems_throws() {
        CreateReadingListDTO dto = buildRecord(CreateReadingListDTO.class, Map.of(
                "title", "List",
                "description", "Desc",
                "userId", 1,
                "itemIds", List.of()
        ));

        assertThrows(RuntimeException.class, () -> service.addReadingList(dto));
    }

    @Test
    void updateReadingList_withItems_updatesAndReturnsDto() {
        ReadingList list = new ReadingList();
        list.setId(1L);
        list.setTitle("Old");
        list.setDescription("Old");
        list.setUser(user);
        list.setItems(List.of(item));

        CreateReadingListDTO dto = buildRecord(CreateReadingListDTO.class, Map.of(
                "title", "New",
                "description", "New",
                "userId", 1,
                "itemIds", List.of(1L)
        ));

        when(readingListRepository.findById(1L)).thenReturn(Optional.of(list));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(readingListRepository.save(any(ReadingList.class))).thenAnswer(inv -> inv.getArgument(0));

        ReadingListDTO result = service.updateReadingList(dto, 1L);

        assertEquals("New", result.title());
    }

    @Test
    void deleteReadingList_deletesAndReturnsDto() {
        ReadingList list = new ReadingList();
        list.setId(1L);
        list.setTitle("List");
        list.setDescription("Desc");
        list.setUser(user);
        list.setItems(List.of(item));

        when(readingListRepository.findById(1L)).thenReturn(Optional.of(list));

        ReadingListDTO result = service.deleteReadingList(1L);

        assertEquals("List", result.title());
        verify(readingListRepository).delete(list);
    }

    @Test
    void isItemInAnyReadingList_delegatesToRepository() {
        when(readingListRepository.existsByItems_Id(1L)).thenReturn(true);
        assertTrue(service.isItemInAnyReadingList(1L));
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