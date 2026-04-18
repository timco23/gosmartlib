package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ItemRepository;
import edu.ap.spring_backend.dto.BookCoverResponse;
import edu.ap.spring_backend.dto.CachedCover;
import edu.ap.spring_backend.exception.EntityNotFoundException;
import edu.ap.spring_backend.jpa.Item;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for fetching, caching, and serving book cover images.
 * It retrieves cover images from external sources (e.g. OpenLibrary, Google Books)
 * and stores them in an in-memory cache to reduce network calls and improve performance.
 * Key features:
 * - In-memory caching with TTL (6 hours)
 * - Automatic fallback between multiple cover sources
 * - ETag generation for HTTP caching
 * - Basic cache size control (clears when limit is exceeded)
 */
@Service
public class BookCoverCacheService {
    private static final long TTL_MS = Duration.ofHours(6).toMillis();
    private static final int MAX_ENTRIES = 2000;

    private final ItemRepository itemRepository;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ConcurrentHashMap<Long, CachedCover> cache = new ConcurrentHashMap<>();

    public BookCoverCacheService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * Retrieves a book cover for a given item ID.
     * Workflow:
     * 1. Loads the item from the database.
     * 2. Builds a list of possible cover URLs.
     * 3. Checks if a valid cached version exists.
     * 4. If not, fetches the cover from external sources and caches it.
     * 5. Supports HTTP caching via ETag (If-None-Match).
     * @param itemId      the ID of the item
     * @param ifNoneMatch optional ETag from the client
     * @return BookCoverResponse containing:
     *         - image bytes (if modified)
     *         - content type
     *         - ETag
     *         - flag indicating if content is unchanged (304 scenario)
     *
     * @throws EntityNotFoundException if item or cover sources are unavailable
     */
    public BookCoverResponse getByItemId(Long itemId, String ifNoneMatch) {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        List<String> candidateUrls = buildCandidateUrls(item);

        if (candidateUrls.isEmpty()) {
            throw new EntityNotFoundException();
        }

        long now = System.currentTimeMillis();
        CachedCover cached = cache.get(itemId);

        boolean validCache = cached != null
                && candidateUrls.contains(cached.url())
                && cached.expiresAt() > now;

        if (!validCache) {
            cached = fetchAndCache(itemId, candidateUrls, cached);
        }

        if (ifNoneMatch != null && ifNoneMatch.contains(cached.etag())) {
            return new BookCoverResponse(null, cached.contentType(), cached.etag(), true);
        }

        return new BookCoverResponse(cached.bytes(), cached.contentType(), cached.etag(), false);
    }

    /**
     * Attempts to fetch a cover image from a list of candidate URLs.
     * - Tries each URL until one succeeds
     * - Stores the result in cache
     * - Clears cache if max size is exceeded
     * - Falls back to previous cached value if still valid
     * @param itemId        the item ID
     * @param candidateUrls list of possible cover URLs
     * @param previous      previous cached entry (optional)
     * @return CachedCover object
     *
     * @throws IllegalStateException if all sources fail and no fallback is available
     */
    private CachedCover fetchAndCache(Long itemId, List<String> candidateUrls, CachedCover previous) {
        RuntimeException last = null;

        for (String url : candidateUrls) {
            try {
                CachedCover entry = fetchFromUrl(url);

                if (cache.size() > MAX_ENTRIES) {
                    cache.clear();
                }
                cache.put(itemId, entry);
                return entry;
            } catch (RuntimeException ex) {
                last = ex;
            }
        }

        if (previous != null && candidateUrls.contains(previous.url())) {
            return previous; // stale fallback
        }

        throw new IllegalStateException("Could not load cover image", last);
    }

    /**
     * Fetches a cover image from a single URL.
     * - Validates URL scheme (http/https only)
     * - Performs HTTP request with timeout
     * - Ensures response is a valid image
     * - Generates an ETag based on image bytes
     * @param coverUrl the URL to fetch the image from
     * @return CachedCover containing image data and metadata
     *
     * @throws IllegalStateException if the request fails or response is invalid
     */
    private CachedCover fetchFromUrl(String coverUrl) {
        try {
            URI uri = URI.create(coverUrl.trim());
            String scheme = uri.getScheme();

            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Invalid cover URL scheme");
            }

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "image/*,*/*;q=0.8")
                    .header("User-Agent", "GoSmartLib/1.0")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] body = response.body();

            if (response.statusCode() < 200 || response.statusCode() >= 300 || body == null || body.length == 0) {
                throw new IllegalStateException("Cover fetch failed with status " + response.statusCode());
            }

            String contentType = response.headers().firstValue("content-type").orElse("");
            if (!contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                throw new IllegalStateException("Cover URL did not return image. content-type=" + contentType);
            }

            String etag = "\"" + DigestUtils.md5DigestAsHex(body) + "\"";

            return new CachedCover(
                    coverUrl,
                    body,
                    contentType,
                    etag,
                    System.currentTimeMillis() + TTL_MS
            );
        } catch (Exception e) {
            throw new IllegalStateException("Could not fetch cover from URL: " + coverUrl, e);
        }
    }

    /**
     * Builds a prioritized list of possible cover URLs for an item.
     * Sources include:
     * - Explicit cover URL from the item
     * - OpenLibrary (large & medium covers)
     * - Google Books API
     * Duplicate URLs are removed while preserving order.
     *
     * @param item the item entity
     * @return list of candidate cover URLs
     */
    private List<String> buildCandidateUrls(Item item) {
        LinkedHashSet<String> urls = new LinkedHashSet<>();

        String sourceCover = normalizeSourceCoverUrl(item.getCover());
        if (sourceCover != null) {
            urls.add(sourceCover);
        }

        String isbn = normalizeIsbn(item.getIsbn());
        if (isbn != null) {
            urls.add("https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg?default=false");
            urls.add("https://covers.openlibrary.org/b/isbn/" + isbn + "-M.jpg?default=false");
            urls.add("https://books.google.com/books/content?vid=ISBN" + isbn + "&printsec=frontcover&img=1&zoom=1&source=gbs_api");
        }

        return new ArrayList<>(urls);
    }

    /**
     * Normalizes a cover URL from the item.
     * - Ensures OpenLibrary URLs include `default=false`
     * - Prevents fallback placeholder images from external APIs
     * @param raw raw cover URL
     * @return normalized URL or null if invalid
     */
    private String normalizeSourceCoverUrl(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String url = raw.trim();

        try {
            URI uri = URI.create(url);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);

            if (!host.contains("covers.openlibrary.org")) {
                return url;
            }

            String query = uri.getQuery();
            if (query == null || query.isBlank()) {
                return url + "?default=false";
            }

            String lowerQuery = query.toLowerCase(Locale.ROOT);
            if (lowerQuery.contains("default=")) {
                return url.replaceAll("(?i)default=(true|1)", "default=false");
            }

            return url + "&default=false";
        } catch (Exception ex) {
            return url;
        }
    }

    /**
     * Cleans and normalizes an ISBN string.
     * - Removes non-numeric characters (except X)
     * - Converts to uppercase
     * @param raw raw ISBN value
     * @return normalized ISBN or null if invalid
     */
    private String normalizeIsbn(String raw) {
        if (raw == null) return null;
        String cleaned = raw.replaceAll("[^0-9Xx]", "");
        if (cleaned.isBlank()) return null;
        return cleaned.toUpperCase(Locale.ROOT);
    }
}