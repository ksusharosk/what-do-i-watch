package com.whatiwatch.api;

import java.util.List;

/**
 * The JSON body for a recommendation request
 * 
 * Example:
 * { "backend": "groq", "genreIds": [28, 53], "decade": "1990s",
 *   "country": "JP", "language": "en", "includeWatched": false }
 */
public record RecommendationRequest (
    String backend,
    List<Integer> genreIds,
    List<String> genreNames,
    List<String> decades,
    List<String> countries,
    String decade,
    String country,
    String language,
    boolean includeWatched, 
    String mood
) {}
