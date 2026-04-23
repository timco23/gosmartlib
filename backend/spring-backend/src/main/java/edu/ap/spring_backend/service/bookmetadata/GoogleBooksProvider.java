package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.dto.GoogleBooksResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Optional;

@Order(1)
@Service
public class GoogleBooksProvider implements BookMetadataProvider {

    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private static final SlidingWindowRateLimiter burstLimiter = new SlidingWindowRateLimiter(100, 100_000);
    private static final SlidingWindowRateLimiter dailyLimiter = new SlidingWindowRateLimiter(1_000, 86_400_000);

    private final RestTemplate restTemplate;

    @Value("${book.metadata.google.enabled}")
    private boolean enabled;

    @Value("${book.metadata.google.api-key}")
    private String apiKey;

    public GoogleBooksProvider(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String getName() { return "GoogleBooks"; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public Optional<BookMetadata> fetchByIsbn(String isbn) {
        try {
            burstLimiter.acquire();
            dailyLimiter.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        try {
            String url = BASE_URL + isbn + (apiKey.isBlank() ? "" : "&key=" + apiKey);
            GoogleBooksResponse response = restTemplate.getForObject(url, GoogleBooksResponse.class);
            if (response != null && response.totalItems() > 0
                    && response.items() != null
                    && !response.items().isEmpty()) {
                return Optional.of(toBookMetadata(response.items().getFirst().volumeInfo()));
            }
        } catch (RestClientException e) {
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


}
