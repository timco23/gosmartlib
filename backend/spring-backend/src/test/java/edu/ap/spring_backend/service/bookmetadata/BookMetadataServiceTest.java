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

    private static final BookMetadata COMPLETE = new BookMetadata(
            "Dune", "Frank Herbert", "A sci-fi epic.", "ENGELS",
            "Ace Books", 412, 1965, "https://covers.example.com/dune.jpg"
    );

    private static final BookMetadata PARTIAL_A = new BookMetadata(
            "Dune", "Frank Herbert", "A sci-fi epic.", null,
            "Ace Books", null, 1965, null
    );

    private static final BookMetadata PARTIAL_B = new BookMetadata(
            null, null, null, "ENGELS",
            null, 412, null, "https://covers.example.com/dune.jpg"
    );

    private static final BookMetadata OTHER = new BookMetadata(
            "Other", "Someone", "Other desc", "fr",
            "Other Pub", 100, 2000, "https://covers.example.com/other.jpg"
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
        assertEquals("ENGELS", result.language());
        assertEquals(412, result.pageCount());
        assertEquals("https://covers.example.com/dune.jpg", result.coverUrl());
    }

    @Test
    void givenBothProvidersPartial_whenFetchByIsbn_thenResultMergedBestEffort() {
        BookMetadataProvider first  = provider(true, Optional.of(PARTIAL_A));
        BookMetadataProvider second = provider(true, Optional.of(PARTIAL_A));
        BookMetadataService service = new BookMetadataService(List.of(first, second));

        BookMetadata result = service.fetchByIsbn(ISBN).orElseThrow();

        assertEquals("Dune", result.title());
        assertNull(result.language());
        assertNull(result.coverUrl());
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
        BookMetadataProvider disabled = provider(false, Optional.of(COMPLETE));
        BookMetadataProvider enabled  = provider(true, Optional.of(PARTIAL_B));
        BookMetadataService service = new BookMetadataService(List.of(disabled, enabled));

        Optional<BookMetadata> result = service.fetchByIsbn(ISBN);

        assertTrue(result.isPresent());
        verify(disabled, never()).fetchByIsbn(anyString());
        verify(enabled).fetchByIsbn(anyString());
    }

    @Test
    void givenFirstProviderEmpty_whenFetchByIsbn_thenSecondProviderDataReturned() {
        BookMetadataProvider first  = provider(true, Optional.empty());
        BookMetadataProvider second = provider(true, Optional.of(COMPLETE));
        BookMetadataService service = new BookMetadataService(List.of(first, second));

        Optional<BookMetadata> result = service.fetchByIsbn(ISBN);

        assertTrue(result.isPresent());
        assertEquals(COMPLETE, result.get());
    }

    @Test
    void givenFirstProviderHasValue_whenFetchByIsbn_thenFirstProviderValueNotOverwritten() {
        BookMetadataProvider first  = provider(true, Optional.of(COMPLETE));
        BookMetadataProvider second = provider(true, Optional.of(OTHER));
        BookMetadataService service = new BookMetadataService(List.of(first, second));

        BookMetadata result = service.fetchByIsbn(ISBN).orElseThrow();

        assertEquals("Dune", result.title());
        assertEquals("Frank Herbert", result.authors());
    }
}