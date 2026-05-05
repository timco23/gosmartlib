package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleBooksProviderTest {

    private GoogleBooksProvider provider;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class, Mockito.RETURNS_SELF);
        when(builder.build()).thenReturn(restTemplate);
        provider = new GoogleBooksProvider(builder);
    }

    @Test
    void toBookMetadata_mapsFields() throws Exception {
        Class<?> volumeInfoClass = Class.forName("edu.ap.spring_backend.dto.GoogleBooksResponse$VolumeInfo");

        Object volumeInfo = buildRecord(volumeInfoClass, Map.of(
                "title", "Dune",
                "authors", List.of("Frank Herbert"),
                "description", "Desc",
                "language", "en",
                "publisher", "Ace",
                "pageCount", 412,
                "publishedDate", "1965-01-01"
        ));

        BookMetadata meta = ReflectionTestUtils.invokeMethod(provider, "toBookMetadata", volumeInfo);

        assertEquals("Dune", meta.title());
        assertEquals("Frank Herbert", meta.authors());
        assertEquals("Desc", meta.description());
        assertEquals("en", meta.language());
        assertEquals("Ace", meta.publisher());
        assertEquals(412, meta.pageCount());
        assertEquals(1965, meta.releaseYear());
    }

    private static Object buildRecord(Class<?> recordClass, Map<String, Object> values) {
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

            Constructor<?> ctor = recordClass.getDeclaredConstructor(types);
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