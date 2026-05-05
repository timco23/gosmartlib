package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dto.BookCoverResponse;
import edu.ap.spring_backend.dto.CachedCover;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookCoverCacheServiceTest {

    @Mock private ItemRepository itemRepository;
    private BookCoverCacheService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new BookCoverCacheService(itemRepository);
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<Long, CachedCover> cache() {
        return (ConcurrentHashMap<Long, CachedCover>) ReflectionTestUtils.getField(service, "cache");
    }

    @Test
    void givenCachedCoverAndMatchingEtag_whenGetByItemId_thenNotModified() {
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setCover("https://example.com/cover.jpg");
        item.setIsbn("9780441568529");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        CachedCover cached = new CachedCover(
                "https://example.com/cover.jpg",
                "img".getBytes(),
                "image/jpeg",
                "\"etag\"",
                System.currentTimeMillis() + 10_000
        );
        cache().put(itemId, cached);

        BookCoverResponse response = service.getByItemId(itemId, "\"etag\"");

        assertTrue(response.notModified());
        assertNull(response.bytes());
        assertEquals("\"etag\"", response.etag());
    }

    @Test
    void givenCachedCoverWithoutEtagMatch_whenGetByItemId_thenReturnsBytes() {
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setCover("https://example.com/cover.jpg");
        item.setIsbn("9780441568529");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        CachedCover cached = new CachedCover(
                "https://example.com/cover.jpg",
                "img".getBytes(),
                "image/jpeg",
                "\"etag\"",
                System.currentTimeMillis() + 10_000
        );
        cache().put(itemId, cached);

        BookCoverResponse response = service.getByItemId(itemId, null);

        assertFalse(response.notModified());
        assertArrayEquals("img".getBytes(), response.bytes());
    }

    @Test
    void givenItemWithoutCoverOrIsbn_whenGetByItemId_thenNotFound() {
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(EntityNotFoundException.class, () -> service.getByItemId(itemId, null));
    }

    @Test
    void givenOpenLibraryUrl_whenNormalizeSourceCoverUrl_thenAddsDefaultFalse() {
        String raw = "https://covers.openlibrary.org/b/isbn/123-L.jpg";
        String normalized = ReflectionTestUtils.invokeMethod(service, "normalizeSourceCoverUrl", raw);

        assertEquals("https://covers.openlibrary.org/b/isbn/123-L.jpg?default=false", normalized);
    }

    @Test
    void givenIsbnWithDashes_whenNormalizeIsbn_thenStripsNonDigits() {
        String normalized = ReflectionTestUtils.invokeMethod(service, "normalizeIsbn", "978-0-441-56852-9");
        assertEquals("9780441568529", normalized);
    }

    @Test
    void givenItem_whenBuildCandidateUrls_thenIncludesSources() {
        Item item = new Item();
        item.setCover("https://example.com/cover.jpg");
        item.setIsbn("9780441568529");

        List<String> urls = ReflectionTestUtils.invokeMethod(service, "buildCandidateUrls", item);

        assertEquals(4, urls.size());
        assertTrue(urls.contains("https://example.com/cover.jpg"));
        assertTrue(urls.contains("https://covers.openlibrary.org/b/isbn/9780441568529-L.jpg?default=false"));
        assertTrue(urls.contains("https://covers.openlibrary.org/b/isbn/9780441568529-M.jpg?default=false"));
    }
}