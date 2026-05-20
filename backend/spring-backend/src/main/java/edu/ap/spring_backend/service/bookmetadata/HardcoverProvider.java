package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.dto.HardcoverResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Order(3)
@Service
public class HardcoverProvider implements BookMetadataProvider {

    private static final String URL = "https://api.hardcover.app/v1/graphql";
    private static final SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(60, 60_000);

    private final RestTemplate restTemplate;

    @Value("${book.metadata.hardcover.enabled}")
    private boolean enabled;

    @Value("${book.metadata.hardcover.api-key:}")
    private String apiKey;

    public HardcoverProvider(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String getName() { return "Hardcover"; }

    @Override
    public boolean isEnabled() { return enabled; }

    /**
     * Fetches book metadata from the Hardcover API using the provided ISBN. Implements rate limiting to comply with API usage policies. Handles various error scenarios gracefully, returning an empty Optional if the fetch fails for any reason (e.g. network issues, API errors, no results found, etc.).
     * @param isbn the ISBN of the book to fetch metadata for
     * @return an Optional containing the BookMetadata if found, or empty if not found or an error occurred
     */
    @Override
    public Optional<BookMetadata> fetchByIsbn(String isbn) {
        try {
            rateLimiter.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        try {
            String query = """
                    { editions(where: { isbn_13: { _eq: "%s" } }) {
                        pages
                        language { language }
                        publisher { name }
                        image { url }
                        book { title release_year description contributions { author { name } } }
                    } }
                    """.formatted(isbn);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!apiKey.isBlank()) {
                headers.set("authorization", apiKey);
            }

            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("query", query), headers);
            HardcoverResponse response = restTemplate.postForObject(URL, request, HardcoverResponse.class);

            if (response == null || response.data() == null || response.data().editions().isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(toBookMetadata(response.data().editions().getFirst()));
        } catch (Exception e) {
            return  Optional.empty();
        }
    }

    /**
     * Converts Hardcover API edition data to our internal BookMetadata format. Handles missing or malformed data gracefully.
     * @param edition
     * @return BookMetadata object with fields mapped from the Hardcover API response. Handles missing or malformed data gracefully.
     */
    private BookMetadata toBookMetadata(HardcoverResponse.Edition edition) {
        HardcoverResponse.Book book = edition.book();

        String authors = null;
        if (book.contributions() != null && !book.contributions().isEmpty()) {
            authors = book.contributions().stream()
                    .map(c -> c.author().name())
                    .filter(n -> n != null && !n.isBlank())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(null);
        }

        String coverUrl = edition.image() != null ? edition.image().url() : null;
        String language = edition.language() != null ? edition.language().language() : null;
        String publisher = edition.publisher() != null ? edition.publisher().name() : null;
        Integer pageCount = (edition.pages() != null && edition.pages() > 0) ? edition.pages() : null;

        return new BookMetadata(
                book.title(),
                authors,
                book.description(),
                language,
                publisher,
                pageCount,
                book.releaseYear(),
                coverUrl
        );
    }
}
