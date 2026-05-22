package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.LendingRepository;
import edu.ap.spring_backend.dao.SchoolItemCopyRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.CampusAvailabilityDTO;
import edu.ap.spring_backend.dto.LendingDTO;
import edu.ap.spring_backend.dto.LendingRequestDTO;
import edu.ap.spring_backend.jpa.Campus;
import edu.ap.spring_backend.jpa.Genre;
import edu.ap.spring_backend.jpa.Item;
import edu.ap.spring_backend.jpa.Lending;
import edu.ap.spring_backend.jpa.ReadingLevel;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.SchoolClass;
import edu.ap.spring_backend.jpa.SchoolItemCopy;
import edu.ap.spring_backend.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class LendingServiceTest {

    @Mock private LendingRepository lendingRepository;
    @Mock private UserRepository userRepository;
    @Mock private SchoolItemCopyRepository copyRepository;
    @Mock private SchoolRepository schoolRepository;

    @InjectMocks
    private LendingService service;

    private User user;
    private School school;
    private Item item;
    private SchoolItemCopy copy;
    private Lending lending;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = mock(User.class);
        when(user.getId()).thenReturn(1);
        when(user.getFirstName()).thenReturn("Ada");
        when(user.getName()).thenReturn("Lovelace");
        when(user.getEmail()).thenReturn("user@example.com");

        school = new School();
        school.setId(1L);
        school.setExtendPeriodDays(7);
        school.setMaxExtends(2); 

        item = new Item();
        item.setId(9L);
        item.setTitle("Dune");
        item.setAuthor("Frank Herbert");

        copy = new SchoolItemCopy();
        copy.setBarcode("BC001");
        copy.setItem(item);

        lending = new Lending();
        lending.setId(10L);
        lending.setBorrower(user);
        lending.setBook(copy);
        lending.setSchool(school);
        lending.setLendingDate(LocalDate.of(2024, 1, 1));
        lending.setReturnDate(LocalDate.of(2024, 1, 10));
        lending.setReturned(false);
        lending.setDateReturned(null);
        lending.setExtended(false);
    }

    @Test
    void getAll_mapsFieldsCorrectly() {
        when(lendingRepository.findAll()).thenReturn(List.of(lending));

        List<LendingDTO> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("Ada Lovelace", result.get(0).borrowerName());
        assertEquals("Dune", result.get(0).bookTitle());
        assertEquals("BC001", result.get(0).copyBarcode());
        assertEquals(9L, result.get(0).itemId());
    }

    @Test
    void addLending_createsAndReturnsDto() {
        LendingRequestDTO req = buildRecord(LendingRequestDTO.class, Map.of(
                "borrowerId", 1,
                "copyBarcode", "BC001",
                "schoolId", 1L,
                "lendingDate", LocalDate.of(2024, 1, 1),
                "returnDate", LocalDate.of(2024, 1, 10)
        ));

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(copyRepository.findByBarcode("BC001")).thenReturn(Optional.of(copy));
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(lendingRepository.save(any(Lending.class))).thenAnswer(inv -> {
            Lending l = inv.getArgument(0);
            l.setId(10L);
            return l;
        });

        LendingDTO result = service.addLending(req);

        assertEquals(10L, result.id());
        assertEquals("Ada Lovelace", result.borrowerName());
        assertEquals("Dune", result.bookTitle());
        assertFalse(result.returned());
        assertFalse(result.extended());
    }

    @Test
    void updateLending_updatesReturnedAndDate() {
        LendingDTO dto = buildRecord(LendingDTO.class, Map.of(
                "returned", true,
                "dateReturned", "2024-01-15T10:00:00"
        ));

        when(lendingRepository.findById(10L)).thenReturn(Optional.of(lending));
        when(lendingRepository.save(any(Lending.class))).thenAnswer(inv -> inv.getArgument(0));

        LendingDTO result = service.updateLending(10L, dto);

        assertTrue(result.returned());
        assertEquals("2024-01-15", result.dateReturned());
    }

    @Test
    void extendLending_whenNotExtended_thenExtends() {
        when(lendingRepository.findById(10L)).thenReturn(Optional.of(lending));
        when(lendingRepository.save(any(Lending.class))).thenAnswer(inv -> inv.getArgument(0));

        LendingDTO result = service.extendLending(10L);

        verify(lendingRepository).save(argThat(l -> l.isExtended() && l.getExtendCount() == 1));
        assertEquals("2024-01-17", result.returnDate());
    }

    @Test
    void extendLending_whenAlreadyExtended_thenThrows() {
        lending.setExtended(true);
        lending.setExtendCount(2);
        when(lendingRepository.findById(10L)).thenReturn(Optional.of(lending));

        assertThrows(ResponseStatusException.class, () -> service.extendLending(10L));
    }

    @Test
    void getById_returnsDto() {
        when(lendingRepository.findById(10L)).thenReturn(Optional.of(lending));

        LendingDTO result = service.getById(10L);

        assertEquals("Dune", result.bookTitle());
        assertEquals("BC001", result.copyBarcode());
    }

    @Test
    void deleteLending_callsRepository() {
        service.deleteLending(10L);
        verify(lendingRepository).deleteById(10L);
    }

    @Test
    void getAllByUser_returnsDtos() {
        when(lendingRepository.findByBorrowerEmail("user@example.com")).thenReturn(List.of(lending));

        List<LendingDTO> result = service.getAllByUser("user@example.com");

        assertEquals(1, result.size());
        assertEquals("Dune", result.get(0).bookTitle());
    }

    @Test
    void getLendingsByBookId_returnsDtos() {
        when(lendingRepository.findByBook_Item_Id(9L)).thenReturn(List.of(lending));

        List<LendingDTO> result = service.getLendingsByBookId(9L);

        assertEquals(1, result.size());
        assertEquals(9L, result.get(0).itemId());
    }

    @Test
    void getEarliestAvailability_singleLending_returnsNlBeDateFormat() {
        Campus campus = new Campus();
        campus.setName("Campus Ellermanstraat");
        copy.setCampus(campus);
        lending.setReturnDate(LocalDate.of(2024, 1, 10));

        when(lendingRepository.findByBook_Item_IdAndReturnedFalse(9L)).thenReturn(List.of(lending));

        List<CampusAvailabilityDTO> result = service.getEarliestAvailabilityPerCampusByBookId(9L);

        assertEquals(1, result.size());
        assertEquals("Campus Ellermanstraat", result.get(0).campusName());
        assertEquals("10 januari 2024", result.get(0).availableFrom());
    }

    @Test
    void getEarliestAvailability_multipleLendingsSameCampus_returnsEarliest() {
        Campus campus = new Campus();
        campus.setName("Campus A");
        copy.setCampus(campus);
        lending.setReturnDate(LocalDate.of(2024, 3, 15));

        SchoolItemCopy copy2 = new SchoolItemCopy();
        copy2.setBarcode("BC002");
        copy2.setItem(item);
        copy2.setCampus(campus);

        Lending lending2 = new Lending();
        lending2.setId(11L);
        lending2.setBook(copy2);
        lending2.setReturnDate(LocalDate.of(2024, 2, 5));
        lending2.setReturned(false);

        when(lendingRepository.findByBook_Item_IdAndReturnedFalse(9L)).thenReturn(List.of(lending, lending2));

        List<CampusAvailabilityDTO> result = service.getEarliestAvailabilityPerCampusByBookId(9L);

        assertEquals(1, result.size());
        assertEquals("Campus A", result.get(0).campusName());
        assertEquals("5 februari 2024", result.get(0).availableFrom());
    }

    @Test
    void getEarliestAvailability_multipleCampuses_returnsOneEntryPerCampus() {
        Campus campusA = new Campus();
        campusA.setName("Campus A");
        Campus campusB = new Campus();
        campusB.setName("Campus B");
        copy.setCampus(campusA);
        lending.setReturnDate(LocalDate.of(2024, 1, 10));

        SchoolItemCopy copy2 = new SchoolItemCopy();
        copy2.setBarcode("BC002");
        copy2.setItem(item);
        copy2.setCampus(campusB);

        Lending lending2 = new Lending();
        lending2.setId(11L);
        lending2.setBook(copy2);
        lending2.setReturnDate(LocalDate.of(2024, 2, 20));
        lending2.setReturned(false);

        when(lendingRepository.findByBook_Item_IdAndReturnedFalse(9L)).thenReturn(List.of(lending, lending2));

        List<CampusAvailabilityDTO> result = service.getEarliestAvailabilityPerCampusByBookId(9L);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.campusName().equals("Campus A") && d.availableFrom().equals("10 januari 2024")));
        assertTrue(result.stream().anyMatch(d -> d.campusName().equals("Campus B") && d.availableFrom().equals("20 februari 2024")));
    }

    @Test
    void getEarliestAvailability_noActiveLendings_returnsEmpty() {
        when(lendingRepository.findByBook_Item_IdAndReturnedFalse(9L)).thenReturn(List.of());

        List<CampusAvailabilityDTO> result = service.getEarliestAvailabilityPerCampusByBookId(9L);

        assertTrue(result.isEmpty());
    }

    @Test
    void fillSnapshot_schoolYear_alwaysSet() {
        Lending l = lendingForSnapshot();
        service.fillSnapshot(l, user, copy);
        assertEquals("2024-2025", l.getSnapSchoolYear());
    }

    @Test
    void fillSnapshot_beforeSeptember_previousSchoolYear() {
        Lending l = new Lending();
        l.setLendingDate(LocalDate.of(2024, 6, 15));
        service.fillSnapshot(l, user, copy);
        assertEquals("2023-2024", l.getSnapSchoolYear());
    }

    @Test
    void fillSnapshot_userWithClass_setsSnapClassName() {
        SchoolClass sc = new SchoolClass();
        sc.setClassName("4A");
        when(user.getClasses()).thenReturn(List.of(sc));

        Lending l = lendingForSnapshot();
        service.fillSnapshot(l, user, copy);

        assertEquals("4A", l.getSnapClassName());
    }

    @Test
    void fillSnapshot_userWithoutClass_snapClassNameIsNull() {
        when(user.getClasses()).thenReturn(List.of());

        Lending l = lendingForSnapshot();
        service.fillSnapshot(l, user, copy);

        assertNull(l.getSnapClassName());
    }

    @Test
    void fillSnapshot_copyWithCampus_setsCampusFields() {
        Campus campus = new Campus();
        campus.setId(5L);
        campus.setName("Campus Noord");
        copy.setCampus(campus);

        Lending l = lendingForSnapshot();
        service.fillSnapshot(l, user, copy);

        assertEquals(5L, l.getSnapCampusId());
        assertEquals("Campus Noord", l.getSnapCampusName());
    }

    @Test
    void fillSnapshot_copyWithoutCampus_campusFieldsAreNull() {
        copy.setCampus(null);

        Lending l = lendingForSnapshot();
        service.fillSnapshot(l, user, copy);

        assertNull(l.getSnapCampusId());
        assertNull(l.getSnapCampusName());
    }

    @Test
    void fillSnapshot_itemWithReadingLevel_setsSnapReadingLevel() {
        ReadingLevel rl = new ReadingLevel("AVI-M6", "Midden groep 6");
        item.setReadingLevel(rl);

        Lending l = lendingForSnapshot();
        service.fillSnapshot(l, user, copy);

        assertEquals("AVI-M6", l.getSnapReadingLevel());
    }

    @Test
    void fillSnapshot_itemWithoutReadingLevel_snapReadingLevelIsNull() {
        item.setReadingLevel(null);

        Lending l = lendingForSnapshot();
        service.fillSnapshot(l, user, copy);

        assertNull(l.getSnapReadingLevel());
    }

    @Test
    void fillSnapshot_itemWithGenres_setsSnapBookGenre() {
        item.setGenres(Set.of(new Genre("Fantasy")));

        Lending l = lendingForSnapshot();
        service.fillSnapshot(l, user, copy);

        assertEquals("Fantasy", l.getSnapBookGenre());
    }

    @Test
    void fillSnapshot_itemWithoutGenres_snapBookGenreIsNull() {
        item.setGenres(Set.of());

        Lending l = lendingForSnapshot();
        service.fillSnapshot(l, user, copy);

        assertNull(l.getSnapBookGenre());
    }

    @Test
    void fillSnapshot_setsTitleAndIsbn() {
        item.setIsbn("978-3-16-148410-0");

        Lending l = lendingForSnapshot();
        service.fillSnapshot(l, user, copy);

        assertEquals("Dune", l.getSnapBookTitle());
        assertEquals("978-3-16-148410-0", l.getSnapBookIsbn());
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

    private Lending lendingForSnapshot() {
        Lending l = new Lending();
        l.setLendingDate(LocalDate.of(2024, 10, 1)); // school year 2024-2025
        return l;
    }
}