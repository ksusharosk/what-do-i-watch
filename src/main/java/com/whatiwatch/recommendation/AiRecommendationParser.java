package com.whatiwatch.recommendation;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatiwatch.config.AiUnavailableException;


/*
 - Parses an AI backend's raw text response into a structured AiRecommendation.
 - the AI is asked to return a JSON array, this parser strips common wrappers and
 fails cleanly (AiUnavailableException) when the content is not usable JSON array.
*/
public final class AiRecommendationParser {
    
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parses AI's response text into a list of recommendations.
     * Skips malformed entires, keeps the valid ones.
     * @throws AiUnavailableException if response isn't a usable JSON
     */
    public List<AiRecommendation> parse(String responseText) throws AiUnavailableException {
        if (responseText == null || responseText.isBlank()) {
            throw new AiUnavailableException("AI", 
                    new IllegalStateException("Empty AI response"));
        }

        String cleaned = stripFences(responseText);

        JsonNode root;
        try {
            root = mapper.readTree(cleaned);
        } catch (Exception e) {
            throw new AiUnavailableException("AI", 
                new IllegalStateException("AI response was not valid JSON: " + cleaned, e));
        }

        if (!root.isArray()) {
            throw new AiUnavailableException("AI", 
                new IllegalStateException("Expected a JSON array, got: " + cleaned));
        }

        List<AiRecommendation> recommendations = new ArrayList<>();
        for (JsonNode item : root) {
            String title = item.path("title").asText("");
            int year = item.path("year").asInt(0);
            String pitch = item.path("pitch").asText();

            // Skip entries missing the essentials rather than failing the whole batch
            if (title.isBlank() || pitch.isBlank()) {
                continue;
            }
            recommendations.add(new AiRecommendation(title, year, pitch));
        }

        return recommendations;

    }

    // Removes markdown code fences the AI may have wrapped the JSON in
    private String stripFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }

}
