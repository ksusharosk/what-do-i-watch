package com.whatiwatch.recommendation;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.whatiwatch.ai.AiBackend;
import com.whatiwatch.ai.AiResponse;
import com.whatiwatch.ai.PromptBuilder;
import com.whatiwatch.config.AiUnavailableException;
import com.whatiwatch.domain.Movie;
import com.whatiwatch.domain.MovieFilter;
import com.whatiwatch.domain.Recommendation;
import com.whatiwatch.domain.user.TasteProfile;

class RecommendationServiceTest {

    // ---- fakes / helpers ----

    /** A fake backend that returns a fixed response, tagged with a name. */
    private static AiBackend fakeBackend(String name, String responseText) {
        return new AiBackend() {
            @Override
            public AiResponse complete(String prompt) {
                return new AiResponse(responseText, name, 0);
            }
            @Override
            public String name() {
                return name;
            }
        };
    }

    /** A fake backend whose complete() fails, to test error propagation. */
    private static AiBackend failingBackend(String name) {
        return new AiBackend() {
            @Override
            public AiResponse complete(String prompt) throws AiUnavailableException {
                throw new AiUnavailableException(name,
                        new IllegalStateException("backend down"));
            }
            @Override
            public String name() {
                return name;
            }
        };
    }

    private Movie movie(int id, String title, int year) {
        return new Movie(id, title, title, "", year,
                List.of(), null, "en", 8.0, 1000, "", List.of(), List.of());
    }

    private RecommendationService serviceWith(
            AiBackend backend,
            BiFunction<String, Integer, List<Movie>> movieSearch) {
        return new RecommendationService(
                new PromptBuilder(),
                new AiBackendRegistry(List.of(backend)),
                new AiRecommendationParser(),
                movieSearch);
    }

    private TasteProfile emptyProfile() {
        return TasteProfile.empty("u1");
    }

    // ---- tests ----

    @Test
    void producesRecommendationsFromAiResponse() throws Exception {
        String aiJson = """
                [
                  {"title": "Parasite", "year": 2019, "pitch": "Dark class satire."},
                  {"title": "Drive", "year": 2011, "pitch": "Neon slow burn."}
                ]
                """;

        // Search returns a matching movie for whatever title it's given
        BiFunction<String, Integer, List<Movie>> search = (title, year) -> {
            int id = title.equals("Parasite") ? 1 : 2;
            return List.of(movie(id, title, year));
        };
        RecommendationService service = serviceWith(fakeBackend("groq", aiJson), search);

        List<Recommendation> recs = service.recommend(emptyProfile(), new MovieFilter(), "groq");

        assertEquals(2, recs.size());
        assertEquals("Parasite", recs.get(0).movie().title());
        assertEquals("Dark class satire.", recs.get(0).aiPitch());
        assertEquals(Recommendation.Feedback.NONE, recs.get(0).feedback());
    }

    @Test
    void skipsSuggestionsWithNoMovieMatch() throws Exception {
        String aiJson = """
                [
                  {"title": "Real Movie", "year": 2019, "pitch": "This one exists."},
                  {"title": "Hallucinated Movie", "year": 2020, "pitch": "TMDB can't find this."}
                ]
                """;

        // Only "Real Movie" resolves; the other returns no matches
        BiFunction<String, Integer, List<Movie>> search = (title, year) ->
                title.equals("Real Movie") ? List.of(movie(1, title, year)) : List.of();

        RecommendationService service = serviceWith(fakeBackend("groq", aiJson), search);

        List<Recommendation> recs = service.recommend(emptyProfile(), new MovieFilter(), "groq");

        assertEquals(1, recs.size());
        assertEquals("Real Movie", recs.get(0).movie().title());
    }

    @Test
    void takesTopSearchResult() throws Exception {
        String aiJson = """
                [ {"title": "Parasite", "year": 2019, "pitch": "The pitch."} ]
                """;

        // Search returns several; the service should take the first
        BiFunction<String, Integer, List<Movie>> search = (title, year) -> List.of(
                movie(1, "Parasite", 2019),
                movie(2, "Parasite Wrong", 1982));

        RecommendationService service = serviceWith(fakeBackend("groq", aiJson), search);

        List<Recommendation> recs = service.recommend(emptyProfile(), new MovieFilter(), "groq");

        assertEquals(1, recs.get(0).movie().id());   // the top result
    }

    @Test
    void emptyAiResponseYieldsNoRecommendations() throws Exception {
        BiFunction<String, Integer, List<Movie>> search =
                (title, year) -> List.of(movie(1, title, year));

        RecommendationService service = serviceWith(fakeBackend("groq", "[]"), search);

        List<Recommendation> recs = service.recommend(emptyProfile(), new MovieFilter(), "groq");

        assertTrue(recs.isEmpty());
    }

    @Test
    void unknownBackendThrows() {
        BiFunction<String, Integer, List<Movie>> search =
                (title, year) -> List.of(movie(1, title, year));

        // Registry only knows "groq", but the user asks for "gemini"
        RecommendationService service = serviceWith(fakeBackend("groq", "[]"), search);

        assertThrows(AiUnavailableException.class,
                () -> service.recommend(emptyProfile(), new MovieFilter(), "gemini"));
    }

    @Test
    void backendFailurePropagates() {
        BiFunction<String, Integer, List<Movie>> search =
                (title, year) -> List.of(movie(1, title, year));

        RecommendationService service = serviceWith(failingBackend("groq"), search);

        assertThrows(AiUnavailableException.class,
                () -> service.recommend(emptyProfile(), new MovieFilter(), "groq"));
    }

    @Test
    void nullProfileRejected() {
        BiFunction<String, Integer, List<Movie>> search =
                (title, year) -> List.of();

        RecommendationService service = serviceWith(fakeBackend("groq", "[]"), search);

        assertThrows(IllegalArgumentException.class,
                () -> service.recommend(null, new MovieFilter(), "groq"));
    }

    @Test
    void nostalgiaRecommendsFromWatchedFilms() throws Exception {
        String aiJson = """
                [
                  {"title": "Spirited Away", "year": 2001, "pitch": "Cozy comfort classic."}
                ]
                """;

        BiFunction<String, Integer, List<Movie>> search =
                (title, year) -> List.of(movie(1, title, year));

        RecommendationService service = serviceWith(fakeBackend("groq", aiJson), search);

        List<Recommendation> recs = service.recommendNostalgia(
                List.of("Spirited Away", "Princess Mononoke"),   // loved
                List.of("Spirited Away", "Some Other Film"),      // watched
                "cozy",
                new MovieFilter(),
                "groq");

        assertEquals(1, recs.size());
        assertEquals("Spirited Away", recs.get(0).movie().title());
        assertEquals("Cozy comfort classic.", recs.get(0).aiPitch());
    }

    @Test
    void nostalgiaSkipsUnresolvableTitles() throws Exception {
        String aiJson = """
                [ {"title": "Nonexistent Film", "year": 1999, "pitch": "..."} ]
                """;

        // Search resolves nothing
        BiFunction<String, Integer, List<Movie>> search = (title, year) -> List.of();

        RecommendationService service = serviceWith(fakeBackend("groq", aiJson), search);

        List<Recommendation> recs = service.recommendNostalgia(
                List.of("Loved Film"), List.of("Watched Film"),
                "intense", new MovieFilter(), "groq");

        assertTrue(recs.isEmpty());
    }

    @Test
    void nostalgiaBackendFailurePropagates() {
        BiFunction<String, Integer, List<Movie>> search =
                (title, year) -> List.of(movie(1, title, year));

        RecommendationService service = serviceWith(failingBackend("groq"), search);

        assertThrows(AiUnavailableException.class,
                () -> service.recommendNostalgia(
                        List.of("A"), List.of("B"), "cozy", new MovieFilter(), "groq"));
    }

}