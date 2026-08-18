package com.whatiwatch.recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.whatiwatch.ai.AiBackend;
import com.whatiwatch.ai.AiResponse;
import com.whatiwatch.ai.PromptBuilder;
import com.whatiwatch.config.AiUnavailableException;
import com.whatiwatch.domain.Movie;
import com.whatiwatch.domain.MovieFilter;
import com.whatiwatch.domain.Recommendation;
import com.whatiwatch.domain.user.TasteProfile;

/**
 * Orchestrates the full recommendation flow: prompt -> AI -> parse -> enrich
 * Works with PromptBuilder, AiBackendRegistry, AiRecommendationParser,
 * looks up movie on TMDB
 * 
 * The movie search is injected as a BiFunction (title, year -> matches)
 * so the service has no direct TMDB/HTTP dependency and stays unit-testable
 */
public final class RecommendationService {

    private final PromptBuilder promptBuilder;
    private final AiBackendRegistry backends;
    private final AiRecommendationParser parser;
    private final BiFunction<String, Integer, List<Movie>> movieSearch;

    public RecommendationService(PromptBuilder promptBuilder,
                                AiBackendRegistry backends,
                                AiRecommendationParser parser,
                                BiFunction<String, Integer, List<Movie>> movieSearch) {
        if (promptBuilder == null || backends == null || parser == null || movieSearch == null) {
            throw new IllegalArgumentException("dependancies cannot be null");
        }
        this.promptBuilder = promptBuilder;
        this.backends = backends;
        this.parser = parser;
        this.movieSearch = movieSearch;
    }

    /**
     * Produces recommendations for a user
     * 
     * @param profile the user's taste profile
     * @param filter the filters chosen for this request
     * @param backendName the user's chosen AI backend (e.g "groq")
     * @return recommendations whose titles resolve to real movies
     * @throws AiUnavailableException if the backend is unknown or the AI fails
     */
    public List<Recommendation> recommend(TasteProfile profile, 
                                        MovieFilter filter, 
                                        String backendName) throws AiUnavailableException {
        if (profile == null) {
            throw new IllegalArgumentException("profile cannot be null");
        }
        if (filter == null) {
            throw new IllegalArgumentException("filter cannot be null");
        }

        // 1. Build the prompt, 2. pick the backend, 3. call the AI
        String prompt = promptBuilder.build(profile, filter);
        AiBackend backend = backends.get(backendName);
        AiResponse response = backend.complete(prompt);

        // 4. Parse the AI's JSON into structured suggestions
        List<AiRecommendation> suggestions = parser.parse(response.text());

        // 5. Enrich each suggestion into a full Recommendation via TMDB
        List<Recommendation> recommendations = new ArrayList<>();
        for (AiRecommendation suggestion : suggestions) {
            Movie movie = findMovie(suggestion);
            if (movie != null) {
                recommendations.add(Recommendation.fresh(movie, suggestion.pitch()));
            }
            // else: skip, if the AI suggested a title TMDB couldn't resolve
        }
        
        return recommendations;

    }


    // Searches TMDB for the suggestion's title/year; returns the top match, or null if none
    private Movie findMovie(AiRecommendation suggestion) {
        List<Movie> matches = movieSearch.apply(suggestion.title(), suggestion.year());
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        return matches.get(0);
    }

}
