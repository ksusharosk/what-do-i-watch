package com.whatiwatch.tmdb;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;

/*
 - Caches TMDB's genre ID -> name mapping
 - TMDB returns genres as numeric IDs, this turns them into human-readable names.
*/
public final class GenreCache {
    
    private final Supplier<JsonNode> genresSource;
    private Map<Integer, String> cache;

    /**
     * @param genresSource supplies TMDB's /genre/movie/list JSON when called
     */
    public GenreCache(Supplier<JsonNode> genresSource) {
        if (genresSource == null) {
            throw new IllegalArgumentException("genresSource cannot be null");
        }
        this.genresSource = genresSource;
    }

    /*
     - Returns the genre name for an ID, or the ID as a string if unknown
     - Fetches and caches the genre list on first call
    */
    public String nameFor(int genreId) {
        ensureLoaded();
        return cache.getOrDefault(genreId, String.valueOf(genreId));
    }

    // Loads the genre map from the source once, then reuses it
    private void ensureLoaded() {
        if (cache != null) {
            return;
        }

        Map<Integer, String> loaded = new HashMap<>();
        JsonNode root = genresSource.get();
        JsonNode genres = root.path("genres");
        
        if (genres.isArray()) {
            for (JsonNode genre : genres) {
                int id = genre.path("id").asInt();
                String name = genre.path("name").asText("");
                if (!name.isBlank()) {
                    loaded.put(id, name);
                }
            }
        }

        this.cache = loaded;
    }

}
