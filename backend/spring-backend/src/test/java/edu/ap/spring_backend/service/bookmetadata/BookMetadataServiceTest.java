package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BookMetadataServiceTest {

    private static final String ISBN = "9780441568529";

    // All 8 fields filled
    private static final BookMetadata COMPLETE = new BookMetadata(
            "Dune", "Frank Herbert", "A sci-fi epic.", "en",
            "Ace Books", 412, 1965, "https://covers.example.com/dune.jpg"
    );

    // Missing language, pageCount, coverUrl
    private static final BookMetadata PARTIAL_A = new BookMetadata(
            "Dune", "Frank Herbert", "A sci-fi epic.", null,
            "Ace Books", null, 1965, null
    );

    // Has exactly the fields PARTIAL_A is missing
    private static final BookMetadata PARTIAL_B = new BookMetadata(
            null, null, null, "en",
            null, 412, null, "https://covers.example.com/dune.jpg"
    );

    private BookMetadataProvider provider(boolean enabled, Optional<BookMetadata> result) {
        BookMetadataProvider p = mock(BookMetadataProvider.class);
        when(p.isEnabled()).thenReturn(enabled);
        when(p.fetchByIsbn(anyString())).thenReturn(result);
        return p;
    }

    @Test
    void givenCompleteFirstProvider_whenFetchByIsbn_thenSecondProviderNotCalled() {
        BookMetadataProvider first  = provider(true, Optional.of(COMPLETE));
        BookMetadataProvider second = provider(true, Optional.of(COMPLETE));
        BookMetadataService service = new BookMetadataService(List.of(first, second));

        Optional<BookMetadata> result = service.fetchByIsbn(ISBN);

        assertTrue(result.isPresent());
        verify(second, never()).fetchByIsbn(anyString());
    }

    @Test
    void givenPartialFirstProvider_whenFetchByIsbn_thenSecondProviderFillsGaps() {
        BookMetadataProvider first  = provider(true, Optional.of(PARTIAL_A));
        BookMetadataProvider second = provider(true, Optional.of(PARTIAL_B));
        BookMetadataService service = new BookMetadataService(List.of(first, second));

        BookMetadata result = service.fetchByIsbn(ISBN).orElseThrow();

        assertNotNull(result.title());
        assertNotNull(result.authors());
        assertNotNull(result.description());
        assertNotNull(result.language());
        assertNotNull(result.publisher());
        assertNotNull(result.pageCount());
        assertNotNull(result.releaseYear());
        assertNotNull(result.coverUrl());
        assertEquals("Dune", result.title());
        assertEquals("en", result.language());
        assertEquals(412, result.pageCount());
        assertEquals("https://covers.example.com/dune.jpg", result.coverUrl());
    }

    @Test
    void givenBothProvidersPartial_whenFetchByIsbn_thenResultMergedBestEffort() {
        BookMetadataProvider first  = provider(true, Optional.of(PARTIAL_A));
        BookMetadataProvider second = provider(true, Optional.of(PARTIAL_A));
        BookMetadataService service = new BookMetadataService(List.of(first, second));

        BookMetadata result = service.fetchByIsbn(ISBN).orElseThrow();

        assertEquals("Dune", result.title());   // kept from first
        assertNull(result.language());          // neither provider had it
        assertNull(result.coverUrl());          // neither provider had it
    }

    @Test
    void givenAllProvidersEmpty_whenFetchByIsbn_thenReturnsEmptyOptional() {
        BookMetadataProvider first  = provider(true, Optional.empty());
        BookMetadataProvider second = provider(true, Optional.empty());
        BookMetadataService service = new BookMetadataService(List.of(first, second));

        assertTrue(service.fetchByIsbn(ISBN).isEmpty());
    }

    @Test
    void givenDisabledProvider_whenFetchByIsbn_thenProviderSkipped() {
        BookMetadataProvider first  = provider(false, Optional.of(COMPLETE));
        BookMetadataProvider second = provider(true,  Optional.of(COMPLETE));
        BookMetadataService service = new BookMetadataService(List.of(first, second));

        service.fetchByIsbn(ISBN);

        verify(first,  never()).fetchByIsbn(anyString());
        verify(second).fetchByIsbn(ISBN);
    }

    @Test
    void givenFirstProviderEmpty_whenFetchByIsbn_thenSecondProviderDataReturned() {
        BookMetadataProvider first  = provider(true, Optional.empty());
        BookMetadataProvider second = provider(true, Optional.of(PARTIAL_A));
        BookMetadataService service = new BookMetadataService(List.of(first, second));

        BookMetadata result = service.fetchByIsbn(ISBN).orElseThrow();

        assertEquals("Dune", result.title());
        assertEquals("Frank Herbert", result.authors());
    }

    @Test
    void givenFirstProviderHasValue_whenFetchByIsbn_thenFirstProviderValueNotOverwritten() {
        BookMetadata withTitle    = new BookMetadata("Original Title", null, null, null, null, null, null, null);
        BookMetadata differentTitle = new BookMetadata("Different Title", "Author", "Desc", "en", "Publisher", 100, 2000, "cover.jpg");

        BookMetadataProvider first  = provider(true, Optional.of(withTitle));
        BookMetadataProvider second = provider(true, Optional.of(differentTitle));
        BookMetadataService service = new BookMetadataService(List.of(first, second));

        BookMetadata result = service.fetchByIsbn(ISBN).orElseThrow();

        assertEquals("Original Title", result.title()); // first provider's value kept
        assertEquals("Author", result.authors());       // filled in from second
    }
}
