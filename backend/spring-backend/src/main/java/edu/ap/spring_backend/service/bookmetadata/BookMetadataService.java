package edu.ap.spring_backend.service.bookmetadata;

import edu.ap.spring_backend.dto.BookMetadata;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
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

        if (!hasAnyData(metadata)) return Optional.empty();

        // Normalize the language to one of: NEDERLANDS, ENGELS, FRANS, DUITS, ANDERE
        metadata = new BookMetadata(
                metadata.title(), metadata.authors(), stripCopyright(metadata.description()),
                normalizeLanguage(metadata.language()),
                metadata.publisher(), metadata.pageCount(), metadata.releaseYear(), metadata.coverUrl()
        );

        return Optional.of(metadata);
    }

    /**
     * Maps raw language values from external APIs to the backend's accepted constants
     * Returns null when the input is null so the caller can fall back to a default.
     */
    private static String normalizeLanguage(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.toLowerCase().trim();

        if (s.equals("nederlands")) return "NEDERLANDS";
        if (s.equals("engels")) return "ENGELS";
        if (s.equals("frans")) return "FRANS";
        if (s.equals("duits")) return "DUITS";
        if (s.equals("andere")) return "ANDERE";

        if (s.equals("nl") || s.equals("nld") || s.equals("dut")
                || s.contains("dutch") || s.contains("flemish") || s.contains("nederlands")) {
            return "NEDERLANDS";
        }
        if (s.equals("en") || s.equals("eng")
                || s.contains("english")) {
            return "ENGELS";
        }
        if (s.equals("fr") || s.equals("fre") || s.equals("fra")
                || s.contains("french") || s.contains("français")) {
            return "FRANS";
        }
        if (s.equals("de") || s.equals("ger") || s.equals("deu")
                || s.contains("german") || s.contains("deutsch")) {
            return "DUITS";
        }

        return "ANDERE";
    }

    private static String stripCopyright(String description) {
        if (description == null) return null;
        String trimmed = description.trim();
        if (trimmed.isEmpty()) return trimmed;

        String lower = trimmed.toLowerCase(Locale.ROOT);
        int idx = indexOfWord(lower, "copyright");
        if (idx == -1) return trimmed;

        return trimmed.substring(0, idx).trim();
    }

     private static int indexOfWord(String text, String word) {
        int idx = text.indexOf(word);
        while (idx != -1) {
            boolean beforeOk = idx == 0 || !Character.isLetterOrDigit(text.charAt(idx - 1));
            int end = idx + word.length();
            boolean afterOk = end >= text.length() || !Character.isLetterOrDigit(text.charAt(end));
            if (beforeOk && afterOk) return idx;
            idx = text.indexOf(word, idx + 1);
        }
        return -1;
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
