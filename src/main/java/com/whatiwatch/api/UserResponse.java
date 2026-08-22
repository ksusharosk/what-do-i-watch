package com.whatiwatch.api;

import java.time.LocalDateTime;
import java.util.List;

import com.whatiwatch.domain.Movie;
import com.whatiwatch.domain.user.User;

/**
 * API-facing view of a User.
 */
public record UserResponse(
        String id,
        String email,
        String displayName,
        LocalDateTime createdAt,
        Preferences preferences
) {
    public record Preferences(
            List<Integer> preferredGenreIds,
            List<Integer> excludedGenreIds,
            List<String> preferredDecades,
            List<String> preferredCountries,
            String preferredLanguage,
            List<Movie.Person> favouriteActors,
            List<Movie.Person> favouriteDirectors,
            String aiBackend
            // note: no encryptedApiKey
    ) {}

    public static UserResponse from(User user) {
        var p = user.preferences();
        return new UserResponse(
                user.id(),
                user.email(),
                user.displayName(),
                user.createdAt(),
                new Preferences(
                        p.preferredGenreIds(),
                        p.excludedGenreIds(),
                        p.preferredDecades(),
                        p.preferredCountries(),
                        p.preferredLanguage(),
                        p.favouriteActors(),
                        p.favouriteDirectors(),
                        p.aiBackend()
                )
        );
    }
}