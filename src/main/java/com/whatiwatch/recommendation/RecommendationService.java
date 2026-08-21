package com.whatiwatch.recommendation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.Set;

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

    private static final int TARGET_COUNT = 5;
    private static final int OVER_REQUEST = 8;
    private static final int MAX_ATTEMPTS = 3;

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
     * Produces up to TARGET_COUNT recommendations, excluding films the user has
     * already watched. Re-requests from the AI (up to MAX_ATTEMPTS) to backfill
     * when watched films are filtered out.
     *
     * @param watchedMovieIds TMDB ids of films the user has watched (to exclude)
     */
    public List<Recommendation> recommend(TasteProfile profile,
                                          MovieFilter filter,
                                          String backendName,
                                          Set<Integer> watchedMovieIds) throws AiUnavailableException {
        if (profile == null) {
            throw new IllegalArgumentException("profile cannot be null");
        }
        if (filter == null) {
            throw new IllegalArgumentException("filter cannot be null");
        }
        Set<Integer> excludeIds = (watchedMovieIds != null) ? watchedMovieIds : Set.of();

        AiBackend backend = backends.get(backendName);

        List<Recommendation> collected = new ArrayList<>();
        List<String> suggestedTitles = new ArrayList<>();   // to exclude on retries
        Set<Integer> collectedIds = new HashSet<>();         // avoid duplicate movies

        for (int attempt = 0; attempt < MAX_ATTEMPTS && collected.size() < TARGET_COUNT; attempt++) {
            String prompt = promptBuilder.build(profile, filter, OVER_REQUEST, suggestedTitles);
            AiResponse response = backend.complete(prompt);
            List<AiRecommendation> suggestions = parser.parse(response.text());

            for (AiRecommendation suggestion : suggestions) {
                suggestedTitles.add(suggestion.title());   // remember, so we don't ask again

                Movie movie = findMovie(suggestion);
                if (movie == null) {
                    continue;   // couldn't resolve on TMDB
                }
                // Skip watched films (unless the user opted to include them)...
                if (!filter.isIncludeWatched() && excludeIds.contains(movie.id())) {
                    continue;
                }
                // ...and skip duplicates we've already collected.
                if (collectedIds.contains(movie.id())) {
                    continue;
                }

                collected.add(Recommendation.fresh(movie, suggestion.pitch()));
                collectedIds.add(movie.id());

                if (collected.size() >= TARGET_COUNT) {
                    break;
                }
            }
        }

        return collected;
    }

    /** Convenience overload with no watched-exclusion (guests / tests). */
    public List<Recommendation> recommend(TasteProfile profile, MovieFilter filter, String backendName)
            throws AiUnavailableException {
        return recommend(profile, filter, backendName, Set.of());
    }

        /**
     * Nostalgia / rewatch recommendations: the AI picks from the user's watched
     * films (preferring loved ones) matching the mood. No watched-exclusion or
     * backfill — the whole point is to draw from films already seen.
     */
    public List<Recommendation> recommendNostalgia(List<String> lovedFilms,
                                                   List<String> watchedFilms,
                                                   String mood,
                                                   MovieFilter filter,
                                                   String backendName) throws AiUnavailableException {
        if (filter == null) {
            throw new IllegalArgumentException("filter cannot be null");
        }

        AiBackend backend = backends.get(backendName);
        String prompt = promptBuilder.buildNostalgia(lovedFilms, watchedFilms, mood, filter, TARGET_COUNT);
        AiResponse response = backend.complete(prompt);
        List<AiRecommendation> suggestions = parser.parse(response.text());

        List<Recommendation> recommendations = new ArrayList<>();
        for (AiRecommendation suggestion : suggestions) {
            Movie movie = findMovie(suggestion);
            if (movie != null) {
                recommendations.add(Recommendation.fresh(movie, suggestion.pitch()));
            }
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
