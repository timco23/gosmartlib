package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.dto.ItemEnrichmentEvent;
import edu.ap.spring_backend.jpa.Item;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Enriches newly imported books with metadata from external APIs on a background thread,
 * after the import transaction commits. Exists as a separate bean so @Async works correctly
 * through the Spring proxy.
 */
@Service
public class BookMetadataEnrichmentService {

    private final ItemRepository itemRepository;
    private final BookMetadataService bookMetadataService;

    public BookMetadataEnrichmentService(ItemRepository itemRepository,
                                         BookMetadataService bookMetadataService) {
        this.itemRepository = itemRepository;
        this.bookMetadataService = bookMetadataService;
    }

    /**
     * Enriches each item in the event with metadata fetched by ISBN.
     * Runs asynchronously on a background thread after the import transaction commits.
     * Only fields returned by the API overwrite the item's default values.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enrich(ItemEnrichmentEvent event) {
        for (ItemEnrichmentEvent.TargetItem targetItem : event.items()) {
            itemRepository.findById(targetItem.itemId()).ifPresent(item ->
                bookMetadataService.fetchByIsbn(targetItem.isbn()).ifPresent(metadata ->
                    updateItem(item, metadata)
                )
            );
        }
    }

    private void updateItem(Item item, BookMetadata metadata) {
        if (metadata.description() != null) item.setDescription(metadata.description());
        if (metadata.publisher()   != null) item.setPublisher(metadata.publisher());
        if (metadata.language()    != null) item.setBookLanguage(metadata.language());
        if (metadata.pageCount()   != null) item.setPageCount(metadata.pageCount());
        if (metadata.releaseYear() != null) item.setReleaseYear(metadata.releaseYear());
        if (metadata.coverUrl()    != null) item.setCover(metadata.coverUrl());
        itemRepository.save(item);
    }
}
