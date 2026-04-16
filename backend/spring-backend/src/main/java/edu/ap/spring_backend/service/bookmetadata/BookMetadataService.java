package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookMetadataService {

    private final List<BookMetadataProvider> providers;

    public BookMetadataService(List<BookMetadataProvider> providers) {
        this.providers = providers;
    }

    /**
     * Tries each enabled provider in @Order priority.
     * Merges partial results across providers until all 8 fields are filled
     * or all providers have been called.
     *
     * @param isbn the 13-digit ISBN to look up
     * @return the merged metadata, or empty if no provider returned anything
     */
    public Optional<BookMetadata> fetchByIsbn(String isbn) {
        BookMetadata metadata = new BookMetadata(null, null, null, null, null, null, null, null);

        for (BookMetadataProvider provider : providers) {
            if (!provider.isEnabled()) continue;
            if (isComplete(metadata)) break;

            Optional<BookMetadata> result = provider.fetchByIsbn(isbn);
            if (result.isPresent()) {
                metadata = merge(metadata, result.get());
            }
        }

        return hasAnyData(metadata) ? Optional.of(metadata) : Optional.empty();
    }

    private static boolean isComplete(BookMetadata m) {
        return m.title() != null && m.authors() != null && m.description() != null
                && m.language() != null && m.publisher() != null
                && m.pageCount() != null && m.releaseYear() != null && m.coverUrl() != null;
    }

    private static boolean hasAnyData(BookMetadata m) {
        return m.title() != null || m.authors() != null || m.description() != null
                || m.language() != null || m.publisher() != null
                || m.pageCount() != null || m.releaseYear() != null || m.coverUrl() != null;
    }

    private static BookMetadata merge(BookMetadata base, BookMetadata other) {
        return new BookMetadata(
                base.title()       != null ? base.title()       : other.title(),
                base.authors()     != null ? base.authors()     : other.authors(),
                base.description() != null ? base.description() : other.description(),
                base.language()    != null ? base.language()    : other.language(),
                base.publisher()   != null ? base.publisher()   : other.publisher(),
                base.pageCount()   != null ? base.pageCount()   : other.pageCount(),
                base.releaseYear() != null ? base.releaseYear() : other.releaseYear(),
                base.coverUrl()    != null ? base.coverUrl()    : other.coverUrl()
        );
    }
}
