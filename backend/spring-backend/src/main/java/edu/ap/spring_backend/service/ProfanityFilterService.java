package edu.ap.spring_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

@Service
public class ProfanityFilterService {

    private final String moderationApiUrl;
    private final String moderationApiKey;
    private final double toxicThreshold;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    public ProfanityFilterService(
            @Value("${moderation.api.url:https://api-inference.huggingface.co/models/unitary/multilingual-toxic-xlm-roberta}") String moderationApiUrl,
            @Value("${moderation.api.key:}") String moderationApiKey,
            @Value("${moderation.toxic-threshold:0.85}") double toxicThreshold
    ) {
        this.moderationApiUrl = moderationApiUrl;
        this.moderationApiKey = moderationApiKey;
        this.toxicThreshold = toxicThreshold;
    }

    public boolean containsProfanity(String input) {
        if (input == null || input.isBlank()) return false;

        if (moderationApiKey == null || moderationApiKey.isBlank()) return true;

        try {
            String payload = """
                {"inputs": %s, "options": {"wait_for_model": true}}
                """.formatted(mapper.writeValueAsString(input));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(moderationApiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + moderationApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) return true;

            JsonNode root = mapper.readTree(res.body());
            if (root.isObject() && (root.has("error") || root.has("message"))) return true;

            if (!root.isArray() || root.isEmpty()) return false;

            JsonNode arr = root.get(0).isArray() ? root.get(0) : root;
            for (JsonNode n : arr) {
                String label = n.path("label").asText("").toLowerCase(Locale.ROOT);
                double score = n.path("score").asDouble(0.0);
                if ((label.contains("toxic") || label.contains("insult") || label.contains("obscene"))
                        && score >= toxicThreshold) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return true; 
        }
    }
}