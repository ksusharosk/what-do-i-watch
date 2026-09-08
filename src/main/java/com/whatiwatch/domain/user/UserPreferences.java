package com.whatiwatch.domain.user;

import java.util.List;

import com.whatiwatch.domain.Movie;

/*
- Stores a user's movie preferences
- Used to personalise recommendations over time
*/
public record UserPreferences(
    List<Integer> preferredGenreIds,
    List<Integer> excludedGenreIds,
    List<String> preferredDecades,
    List<String> preferredCountries,
    String preferredLanguage,
    List<Movie.Person> favouriteActors,
    List<Movie.Person> favouriteDirectors,
    String aiBackend,
    String encryptedApiKey, 
    boolean hasCompletedOnboarding,
    String avatarId
) {
    /*
    - Returns sensible defaults for a new user
    - No filters set, English language, Groq as default backend
    */
   public static UserPreferences defaults() {
    return new UserPreferences(
    List.of(),
    List.of(), 
    List.of(), 
    List.of(),
    "en", 
    List.of(), 
    List.of(),
    "groq", 
    null, false, null
    );
   }
   //Returns a copy with a different preferred language
   public UserPreferences withLanguage(String language) {
        return new UserPreferences(
        preferredGenreIds, 
        excludedGenreIds, 
        preferredDecades, 
        preferredCountries, 
        language, 
        favouriteActors, 
        favouriteDirectors, 
        aiBackend, 
        encryptedApiKey, 
        hasCompletedOnboarding,
        avatarId
    );
   }

   //Returns a copy with a different AI backend
   public UserPreferences withAiBackend(String aiBackend, String encryptedApiKey) {
        return new UserPreferences(
        preferredGenreIds, 
        excludedGenreIds, 
        preferredDecades, 
        preferredCountries, 
        preferredLanguage, 
        favouriteActors, 
        favouriteDirectors, 
        aiBackend, 
        encryptedApiKey, 
        hasCompletedOnboarding,
        avatarId
    );
   }

   /** Returns a copy marking onboarding as completed */
   public UserPreferences withOnboardingCompleted() {
        return new UserPreferences(
        preferredGenreIds, 
        excludedGenreIds, 
        preferredDecades, 
        preferredCountries, 
        preferredLanguage, 
        favouriteActors, 
        favouriteDirectors, 
        aiBackend, 
        encryptedApiKey, 
        true,
        avatarId
        );
    }

    /** Returns a copy with a different avatar. */
    public UserPreferences withAvatar(String avatarId) {
        return new UserPreferences(
        preferredGenreIds, 
        excludedGenreIds, 
        preferredDecades, 
        preferredCountries, 
        preferredLanguage, 
        favouriteActors, 
        favouriteDirectors, 
        aiBackend, 
        encryptedApiKey, 
        hasCompletedOnboarding,
        avatarId
        );
    }
}
