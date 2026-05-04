package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;
import edu.ap.spring_backend.dto.OpenLibraryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Order(2)
@Service
public class OpenLibraryProvider implements BookMetadataProvider {

    private static final String BASE_URL =
            "https://openlibrary.org/api/books?bibkeys=ISBN:%s&format=json&jscmd=data";
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(\\d{4})\\b");
    private static final ParameterizedTypeReference<Map<String, OpenLibraryResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(100, 300_000);

    private final RestTemplate restTemplate;

    @Value("${book.metadata.openlibrary.enabled}")
    private boolean enabled;

    public OpenLibraryProvider(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String getName() { return "OpenLibrary"; }

    @Override
    public boolean isEnabled() { return enabled; }

    /**
     * Fetches book metadata from the Open Library API using the provided ISBN. Implements rate limiting to comply with API usage policies. Handles various error scenarios gracefully, returning an empty Optional if the fetch fails for any reason (e.g. network issues, API errors, no results found, etc.).
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
            Map<String, OpenLibraryResponse> body = restTemplate.exchange(
                    String.format(BASE_URL, isbn), HttpMethod.GET, null, RESPONSE_TYPE
            ).getBody();

            if (body == null) return Optional.empty();

            OpenLibraryResponse bookData = body.get("ISBN:" + isbn);
            if (bookData == null) return Optional.empty();

            return Optional.of(toBookMetadata(bookData));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Converts Open Library API response data to our internal BookMetadata format. Handles missing or malformed data gracefully.
     * @param data
     * @return BookMetadata object with fields mapped from the Open Library API response. Handles missing or malformed data gracefully.
     */
    private BookMetadata toBookMetadata(OpenLibraryResponse data) {
        String authors = null;
        if (data.authors() != null && !data.authors().isEmpty()) {
            authors = data.authors().stream()
                    .map(OpenLibraryResponse.Author::name)
                    .filter(n -> n != null && !n.isBlank())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(null);
        }

        String publisher = null;
        if (data.publishers() != null && !data.publishers().isEmpty()) {
            publisher = data.publishers().getFirst().name();
        }

        String coverUrl = null;
        if (data.cover() != null) {
            coverUrl = data.cover().medium();
        }

        Integer releaseYear = null;
        if (data.publishDate() != null) {
            Matcher m = YEAR_PATTERN.matcher(data.publishDate());
            if (m.find()) {
                try { releaseYear = Integer.parseInt(m.group(1)); }
                catch (NumberFormatException ignored) {}
            }
        }

        Integer pageCount = (data.numberOfPages() != null && data.numberOfPages() > 0)
                ? data.numberOfPages() : null;

        // Open Library (jscmd=data) does not return description or language
        return new BookMetadata(
                data.title(),
                authors,
                null,
                null,
                publisher,
                pageCount,
                releaseYear,
                coverUrl
        );
    }
}
