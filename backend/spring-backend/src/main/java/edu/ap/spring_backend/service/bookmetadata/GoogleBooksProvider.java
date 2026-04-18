package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.dto.GoogleBooksResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Order(1)
@Service
public class GoogleBooksProvider implements BookMetadataProvider {

    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private static final long DELAY_MS = 1000;
    private static final Object LOCK = new Object();
    private static long lastCallTime = 0;

    private final RestTemplate restTemplate;

    @Value("${book.metadata.google.enabled}")
    private boolean enabled;

    @Value("${book.metadata.google.api-key}")
    private String apiKey;

    public GoogleBooksProvider(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public String getName() { return "GoogleBooks"; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public Optional<BookMetadata> fetchByIsbn(String isbn) {
        try {
            throttle();
            String url = BASE_URL + isbn + (apiKey.isBlank() ? "" : "&key=" + apiKey);
            GoogleBooksResponse response = restTemplate.getForObject(url, GoogleBooksResponse.class);
            if (response != null && response.totalItems() > 0
                    && response.items() != null
                    && !response.items().isEmpty()) {
                return Optional.of(toBookMetadata(response.items().getFirst().volumeInfo()));
            }
        } catch (Exception e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private BookMetadata toBookMetadata(GoogleBooksResponse.VolumeInfo info) {
        String authors = null;
        if (info.authors() != null && !info.authors().isEmpty()) {
            authors = String.join(", ", info.authors());
        }

        Integer releaseYear = null;
        if (info.publishedDate() != null && info.publishedDate().length() >= 4) {
            try {
                releaseYear = Integer.parseInt(info.publishedDate().substring(0, 4));
            } catch (NumberFormatException ignored) {}
        }

        String coverUrl = null;
        if (info.imageLinks() != null) {
            coverUrl = info.imageLinks().thumbnail();
        }

        return new BookMetadata(
                info.title(),
                authors,
                info.description(),
                info.language(),
                info.publisher(),
                info.pageCount() > 0 ? info.pageCount() : null,
                releaseYear,
                coverUrl
        );
    }

    /**
     * Enforces a 1-second gap between calls to stay within Google Books rate limit.
     * Needed during bulk CSV imports where many ISBNs are looked up in sequence.
     */
    private void throttle() {
        synchronized (LOCK) {
            long now = System.currentTimeMillis();
            long wait = DELAY_MS - (now - lastCallTime);
            if (wait > 0) {
                try { Thread.sleep(wait); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            lastCallTime = System.currentTimeMillis();
        }
    }
}
