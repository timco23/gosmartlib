package edu.ap.spring_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
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

    private static final String[] MODERATION_CATEGORIES = {
        "sexual", "hate_and_discrimination", "violent_crimes", "harassment", "selfharm"
    };

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
        String text = normalizeInput(input);
        if (text == null) return false;

        if (isBlockedByLocalList(text)) return true;
        if (shouldSkipRemoteModeration(text)) return false;

        return checkRemoteModeration(text);
    }

    private String normalizeInput(String input) {
        if (input == null) return null;
        String text = input.trim();
        return text.isEmpty() ? null : text;
    }

    private boolean shouldSkipRemoteModeration(String text) {
        return text.length() < MIN_INPUT_LENGTH || moderationApiKey == null || moderationApiKey.isBlank();
    }

    private boolean checkRemoteModeration(String text) {
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
            if (!isSuccess(res)) return false;

            return hasHighScore(res.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException e) {
            return false;
        }
    }

     private boolean isSuccess(HttpResponse<?> res) {
        return res.statusCode() >= 200 && res.statusCode() < 300;
    }

    private boolean hasHighScore(String body) throws IOException {
        JsonNode root = mapper.readTree(body);
        JsonNode results = root.path("results");
        if (!results.isArray() || results.isEmpty()) return false;

        JsonNode scores = results.get(0).path("category_scores");
        return exceedsThreshold(scores, 0.5);
    }

    private boolean exceedsThreshold(JsonNode scores, double threshold) {
        for (String cat : MODERATION_CATEGORIES) {
            if (scores.path(cat).asDouble(0.0) >= threshold) return true;
        }
        return false;
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

        var words = Arrays.stream(raw.split("[,;]+")) 
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Pattern::quote)
            .collect(Collectors.toList());

        if (words.isEmpty()) return null;

        String regex = "(?<![\\p{L}\\p{N}])(" + String.join("|", words) + ")(?![\\p{L}\\p{N}])";
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
    }
}