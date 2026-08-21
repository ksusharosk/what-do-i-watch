package com.whatiwatch.api;

import java.util.List;

/**
 * Request body for updating user preferences. All fields optional — only the
 * non-null ones are applied (partial update).
 */
public record PreferencesRequest(
        List<Integer> preferredGenreIds,
        List<Integer> excludedGenreIds,
        List<String> preferredDecades,
        List<String> preferredCountries,
        String preferredLanguage,
        String aiBackend
) {}