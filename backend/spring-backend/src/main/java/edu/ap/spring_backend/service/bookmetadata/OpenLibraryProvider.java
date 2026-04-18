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

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Order(2)
@Service
public class OpenLibraryProvider implements BookMetadataProvider {

    private static final String BASE_URL =
            "https://openlibrary.org/api/books?bibkeys=ISBN:%s&format=json&jscmd=data";
    private static final long DELAY_MS = 3000;
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(\\d{4})\\b");
    private static final ParameterizedTypeReference<Map<String, OpenLibraryResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final Object LOCK = new Object();
    private static long lastCallTime = 0;

    private final RestTemplate restTemplate;

    @Value("${book.metadata.openlibrary.enabled}")
    private boolean enabled;

    public OpenLibraryProvider(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public String getName() { return "OpenLibrary"; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public Optional<BookMetadata> fetchByIsbn(String isbn) {
        try {
            throttle();
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
     * Enforces a 3-second gap between calls to stay within Open Library's rate limit of 100 req/5 min.
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
