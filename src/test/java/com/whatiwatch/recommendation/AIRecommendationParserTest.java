package com.whatiwatch.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.whatiwatch.config.AiUnavailableException;

class AiRecommendationParserTest {

    private final AiRecommendationParser parser = new AiRecommendationParser();

    @Test
    void parsesCleanJsonArray() throws Exception {
        String json = """
                [
                  {"title": "Parasite", "year": 2019, "pitch": "Dark class satire you'll love."},
                  {"title": "Oldboy", "year": 2003, "pitch": "Brutal, twisty Korean thriller."}
                ]
                """;

        List<AiRecommendation> recs = parser.parse(json);

        assertEquals(2, recs.size());
        assertEquals("Parasite", recs.get(0).title());
        assertEquals(2019, recs.get(0).year());
        assertEquals("Oldboy", recs.get(1).title());
    }

    @Test
    void stripsMarkdownFences() throws Exception {
        // The AI wrapped it in a ```json fence despite being told not to
        String fenced = """
```json
                [ {"title": "Drive", "year": 2011, "pitch": "Neon-lit slow burn."} ]
```
                """;

        List<AiRecommendation> recs = parser.parse(fenced);

        assertEquals(1, recs.size());
        assertEquals("Drive", recs.get(0).title());
    }

    @Test
    void stripsPlainFences() throws Exception {
        // Fence with no language tag
        String fenced = """
            [ {"title": "Drive", "year": 2011, "pitch": "Neon-lit slow burn."} ]
""";

        List<AiRecommendation> recs = parser.parse(fenced);

        assertEquals(1, recs.size());
        assertEquals("Drive", recs.get(0).title());
    }

    @Test
    void skipsEntriesMissingTitleOrPitch() throws Exception {
        String json = """
                [
                  {"title": "Good Movie", "year": 2020, "pitch": "Solid pick."},
                  {"title": "", "year": 2020, "pitch": "No title so skipped."},
                  {"title": "No Pitch Movie", "year": 2020, "pitch": ""}
                ]
                """;

        List<AiRecommendation> recs = parser.parse(json);

        // Only the first is complete
        assertEquals(1, recs.size());
        assertEquals("Good Movie", recs.get(0).title());
    }

    @Test
    void missingYearDefaultsToZero() throws Exception {
        String json = """
                [ {"title": "Yearless", "pitch": "The AI forgot the year."} ]
                """;

        List<AiRecommendation> recs = parser.parse(json);

        assertEquals(1, recs.size());
        assertEquals(0, recs.get(0).year());
    }

    @Test
    void emptyArrayReturnsEmptyList() throws Exception {
        List<AiRecommendation> recs = parser.parse("[]");

        assertTrue(recs.isEmpty());
    }

    @Test
    void nullOrBlankResponseThrows() {
        assertThrows(AiUnavailableException.class, () -> parser.parse(null));
        assertThrows(AiUnavailableException.class, () -> parser.parse("   "));
    }

    @Test
    void nonJsonResponseThrows() {
        // The AI ignored instructions and just wrote prose
        assertThrows(AiUnavailableException.class,
                () -> parser.parse("Sure! Here are some great movies for you to watch."));
    }

    @Test
    void nonArrayJsonThrows() {
        // Valid JSON, but an object instead of an array
        assertThrows(AiUnavailableException.class,
                () -> parser.parse("{\"title\": \"Solo Movie\"}"));
    }
}