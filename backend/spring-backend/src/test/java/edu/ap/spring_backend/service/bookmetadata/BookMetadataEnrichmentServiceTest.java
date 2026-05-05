package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.dto.ItemEnrichmentEvent;
import edu.ap.spring_backend.jpa.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookMetadataEnrichmentServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookMetadataService bookMetadataService;

    private BookMetadataEnrichmentService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new BookMetadataEnrichmentService(itemRepository, bookMetadataService);
    }

    @Test
    void enrich_updatesItemFields_whenMetadataPresent() {
        Item item = new Item();
        item.setDescription("old");
        item.setPublisher("oldPub");
        item.setBookLanguage("OLD");
        item.setPageCount(10);
        item.setReleaseYear(1900);
        item.setCover("oldCover");

        BookMetadata metadata = new BookMetadata(
                null, null, "new desc", "ENGELS", "newPub", 100, 2000, "newCover"
        );

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookMetadataService.fetchByIsbn("isbn")).thenReturn(Optional.of(metadata));

        service.enrich(new ItemEnrichmentEvent(List.of(new ItemEnrichmentEvent.TargetItem(1L, "isbn"))));

        assertEquals("new desc", item.getDescription());
        assertEquals("newPub", item.getPublisher());
        assertEquals("ENGELS", item.getBookLanguage());
        assertEquals(100, item.getPageCount());
        assertEquals(2000, item.getReleaseYear());
        assertEquals("newCover", item.getCover());

        verify(itemRepository).save(item);
    }

    @Test
    void enrich_doesNothing_whenNoMetadata() {
        Item item = new Item();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookMetadataService.fetchByIsbn("isbn")).thenReturn(Optional.empty());

        service.enrich(new ItemEnrichmentEvent(List.of(new ItemEnrichmentEvent.TargetItem(1L, "isbn"))));

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void enrich_doesNothing_whenItemMissing() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        service.enrich(new ItemEnrichmentEvent(List.of(new ItemEnrichmentEvent.TargetItem(1L, "isbn"))));

        verify(bookMetadataService, never()).fetchByIsbn(anyString());
        verify(itemRepository, never()).save(any(Item.class));
    }
}