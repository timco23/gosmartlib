package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.dto.HardcoverResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HardcoverProviderTest {

    private HardcoverProvider provider;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class, Mockito.RETURNS_SELF);
        when(builder.build()).thenReturn(restTemplate);
        provider = new HardcoverProvider(builder);
    }

    @Test
    void toBookMetadata_mapsFields() {
        HardcoverResponse.Edition edition = new HardcoverResponse.Edition(
                300,
                new HardcoverResponse.Language("en"),
                new HardcoverResponse.Publisher("Ace"),
                new HardcoverResponse.Image("https://img"),
                new HardcoverResponse.Book(
                        "Dune",
                        1965,
                        "Desc",
                        List.of(new HardcoverResponse.Contribution(
                                new HardcoverResponse.Author("Frank Herbert")
                        ))
                )
        );

        BookMetadata meta = ReflectionTestUtils.invokeMethod(provider, "toBookMetadata", edition);

        assertEquals("Dune", meta.title());
        assertEquals("Frank Herbert", meta.authors());
        assertEquals("Desc", meta.description());
        assertEquals("en", meta.language());
        assertEquals("Ace", meta.publisher());
        assertEquals(300, meta.pageCount());
        assertEquals(1965, meta.releaseYear());
        assertEquals("https://img", meta.coverUrl());
    }

    @Test
    void toBookMetadata_handlesMissingFields() {
        HardcoverResponse.Edition edition = new HardcoverResponse.Edition(
                0,
                null,
                null,
                null,
                new HardcoverResponse.Book("Title", null, null, List.of())
        );

        BookMetadata meta = ReflectionTestUtils.invokeMethod(provider, "toBookMetadata", edition);

        assertNull(meta.pageCount());
        assertNull(meta.language());
        assertNull(meta.publisher());
        assertNull(meta.coverUrl());
    }
}