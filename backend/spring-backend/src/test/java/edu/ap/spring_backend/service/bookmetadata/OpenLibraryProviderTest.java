package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.dto.OpenLibraryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenLibraryProviderTest {

    private OpenLibraryProvider provider;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class, Mockito.RETURNS_SELF);
        when(builder.build()).thenReturn(restTemplate);
        provider = new OpenLibraryProvider(builder);
    }

    @Test
    void toBookMetadata_mapsFields() {
        OpenLibraryResponse data = new OpenLibraryResponse(
                "Dune",
                List.of(new OpenLibraryResponse.Author("Frank Herbert")),
                412,
                List.of(new OpenLibraryResponse.Publisher("Ace")),
                "1965",
                new OpenLibraryResponse.Cover("s", "m", "l")
        );

        BookMetadata meta = ReflectionTestUtils.invokeMethod(provider, "toBookMetadata", data);

        assertEquals("Dune", meta.title());
        assertEquals("Frank Herbert", meta.authors());
        assertEquals("Ace", meta.publisher());
        assertEquals(412, meta.pageCount());
        assertEquals(1965, meta.releaseYear());
        assertEquals("m", meta.coverUrl());
        assertNull(meta.description());
        assertNull(meta.language());
    }

    @Test
    void toBookMetadata_handlesMissingValues() {
        OpenLibraryResponse data = new OpenLibraryResponse(
                null, null, 0, null, "unknown", null
        );

        BookMetadata meta = ReflectionTestUtils.invokeMethod(provider, "toBookMetadata", data);

        assertNull(meta.authors());
        assertNull(meta.publisher());
        assertNull(meta.pageCount());
        assertNull(meta.releaseYear());
        assertNull(meta.coverUrl());
    }
}