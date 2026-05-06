package edu.ap.spring_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProfanityFilterService {

    private final String moderationApiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();
    private static final int MIN_INPUT_LENGTH = 3;
    private final Pattern blocklistPattern;

    public ProfanityFilterService(
            @Value("${moderation.api.key:}") String moderationApiKey,
            @Value("${moderation.blocklist:}") String blocklist
    ) {
        this.moderationApiKey = moderationApiKey;
        this.blocklistPattern = buildBlocklistPattern(blocklist);
    }

    public boolean containsProfanity(String input) {
        if (input == null) return false;
        String text = input.trim();
        if (text.isEmpty()) return false;

        if (isBlockedByLocalList(text)) return true;
        if (text.length() < MIN_INPUT_LENGTH) return false;
        if (moderationApiKey == null || moderationApiKey.isBlank()) return false;

        try {
            String payload = mapper.writeValueAsString(
                java.util.Map.of("input", text, "model", "mistral-moderation-latest")
            );

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mistral.ai/v1/moderations"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + moderationApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) return false;

            JsonNode root = mapper.readTree(res.body());
            JsonNode results = root.path("results");
            if (results.isArray() && !results.isEmpty()) {
                JsonNode scores = results.get(0).path("category_scores");
                double threshold = 0.5;
                for (String cat : new String[]{"sexual", "hate_and_discrimination", "violent_crimes", "harassment", "selfharm"}) {
                    if (scores.path(cat).asDouble(0.0) >= threshold) return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isBlockedByLocalList(String text) {
        if (blocklistPattern == null) return false;
        if (blocklistPattern.matcher(text).find()) return true;
        String normalized = normalizeText(text);
        return blocklistPattern.matcher(normalized).find();
    }
    
    private static String normalizeText(String text) {
        String noSpacedLetters = text.replaceAll("(?<=\\b\\S)\\s(?=\\S\\b)", "");
        String stripped = noSpacedLetters.replaceAll("[^\\p{L}\\p{N}\\s]", "");
        String deduped = stripped.replaceAll("(\\p{L})\\1+", "$1");
        return deduped.toLowerCase(Locale.ROOT);
    }

   private static Pattern buildBlocklistPattern(String raw) {
        if (raw == null || raw.isBlank()) return null;

        var words = Arrays.stream(raw.split("[,;\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Pattern::quote)
                .collect(Collectors.toList());

        if (words.isEmpty()) return null;

        String regex = "(?<![\\p{L}\\p{N}])(" + String.join("|", words) + ")(?![\\p{L}\\p{N}])";
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
    }
}