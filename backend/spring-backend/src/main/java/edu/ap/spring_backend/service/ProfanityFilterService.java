package edu.ap.spring_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for detecting profanity in text input.
 * This service normalizes input (case-insensitive, removes diacritics),
 * tokenizes it, and checks against a configurable set of blocked words.
 * It also detects split or obfuscated words by combining tokens.
 */
@Service
public class ProfanityFilterService {

    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^\\p{L}\\p{N}]+");

    private final Set<String> blockedWords;
    private final Set<String> allowContainsWords;

     /**
     * Initializes the profanity filter with configured blocked and allowed words.
     * @param configuredWords comma-separated blocked words
     * @param configuredAllowContainsWords comma-separated allowed words
     */
    public ProfanityFilterService(
            @Value("${review.profanity.words:}") String configuredWords,
            @Value("${review.profanity.allow-contains:}") String configuredAllowContainsWords
    ) {
        this.blockedWords = parseCsv(configuredWords);
        this.allowContainsWords = parseCsv(configuredAllowContainsWords);
    }

     /**
     * Checks whether the given input contains profanity.
     * The input is normalized, split into tokens, and also checked in combined
     * token forms (e.g. "mother fucker" → "motherfucker").
     * @param input the text to analyze
     * @return {@code true} if profanity is detected, otherwise {@code false}
     */
    public boolean containsProfanity(String input) {
        if (input == null || input.isBlank() || blockedWords.isEmpty()) {
            return false;
        }

        String normalized = normalize(input);
        List<String> tokens = Arrays.stream(TOKEN_SPLIT.split(normalized))
                .map(this::canonicalize)
                .filter(s -> !s.isBlank())
                .toList();

        for (int i = 0; i < tokens.size(); i++) {
            String t1 = tokens.get(i);

            // single token
            if (!allowContainsWords.contains(t1) && containsBlocked(t1)) {
                return true;
            }

            // 2-token combine (ex. "mother fucker" -> "motherfucker")
            if (i + 1 < tokens.size()) {
                String t2 = t1 + tokens.get(i + 1);
                if (containsBlocked(t2)) return true;
            }

            // 3-token combine (ex. "mo ther fucker")
            if (i + 2 < tokens.size()) {
                String t3 = t1 + tokens.get(i + 1) + tokens.get(i + 2);
                if (containsBlocked(t3)) return true;
            }
        }

        return false;
    }

     /**
     * Checks if a candidate string contains any blocked word.
     * @param candidate the normalized token or combined token
     * @return {@code true} if a blocked word is matched
     */
     private boolean containsBlocked(String candidate) {
        for (String badWord : blockedWords) {
            if (isProfaneToken(candidate, badWord)) return true;
        }
        return false;
    }

     /**
     * Determines if a token matches a blocked word.
     * @param token the token to check
     * @param badWord the blocked word
     * @return {@code true} if the token is considered profane
     */
    private boolean isProfaneToken(String token, String badWord) {
        // Exact match (always block)
        if (token.equals(badWord)) return true;

        // Prefix match: “fucking” contains “fuck”, ‘asshole’ contains “ass” as a prefix
        if (token.startsWith(badWord) && token.length() > badWord.length()) return true;

        // Infix/substring only for longer words (avoids false positives such as “class” with “ass”)
        return badWord.length() >= 4 && token.contains(badWord);
    }

    /**
     * Parses a comma-separated string into a normalized set of words.
     * @param csv comma-separated values
     * @return set of normalized, non-empty strings
     */
    private Set<String> parseCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    /**
     * Normalizes a string by removing diacritics and converting to lowercase.
     * @param value input string
     * @return normalized string
     */
    private String normalize(String value) {
        String nfd = Normalizer.normalize(value, Normalizer.Form.NFD);
        return DIACRITICS.matcher(nfd).replaceAll("").toLowerCase(Locale.ROOT);
    }

    /**
     * Canonicalizes a string by normalizing it and removing all non-alphanumeric characters.
     * This helps detect obfuscated profanity such as "f*ck" → "fck".
     * @param value input string
     * @return cleaned, alphanumeric-only string
     */
    private String canonicalize(String value) {
        return NON_ALNUM.matcher(normalize(value)).replaceAll("");
    }
}