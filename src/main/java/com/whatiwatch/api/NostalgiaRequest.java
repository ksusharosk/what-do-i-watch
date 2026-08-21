package com.whatiwatch.api;

import java.util.List;

/**
 * Request body for nostalgia / rewatch recommendations.
 */
public record NostalgiaRequest(
        String backend,
        String mood,
        List<Integer> genreIds,
        String decade,
        String country,
        String language
) {}