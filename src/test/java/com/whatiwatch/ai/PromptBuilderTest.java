package com.whatiwatch.ai;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.whatiwatch.domain.MovieFilter;
import com.whatiwatch.domain.user.TasteProfile;

class PromptBuilderTest {

    private final PromptBuilder builder = new PromptBuilder();

    @Test
    void nullProfileIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> builder.build(null, new MovieFilter()));
    }

    @Test
    void nullFilterIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> builder.build(TasteProfile.empty("u1"), null));
    }

    @Test
    void newUserPromptUsesGenericFallback() {
        String prompt = builder.build(TasteProfile.empty("u1"), new MovieFilter());

        // TasteProfile.toPromptContext() returns the new-user fallback
        assertTrue(prompt.contains("New user"));
    }

    @Test
    void promptIncludesTasteContext() {
        TasteProfile profile = profileWithLovedFilms();

        String prompt = builder.build(profile, new MovieFilter());

        // The loved-films line from toPromptContext() should be present
        assertTrue(prompt.contains("Parasite"));
        assertTrue(prompt.contains("Oldboy"));
    }

    @Test
    void promptIncludesFilterDetails() {
        MovieFilter filter = new MovieFilter()
                .withGenre(28)
                .withDecade("1990s")
                .withCountry("JP");

        String prompt = builder.build(profileWithLovedFilms(), filter);

        assertTrue(prompt.contains("1990s"));
        assertTrue(prompt.contains("JP"));
        assertTrue(prompt.contains("28"));
    }

    @Test
    void promptAsksForJsonArray() {
        String prompt = builder.build(profileWithLovedFilms(), new MovieFilter());

        // The prompt should instruct the AI to return JSON with the expected fields
        assertTrue(prompt.contains("JSON"));
        assertTrue(prompt.contains("\"title\""));
        assertTrue(prompt.contains("\"pitch\""));
    }

    private TasteProfile profileWithLovedFilms() {
        return new TasteProfile(
                "u1",
                List.of(),               // likedGenres
                List.of(),               // dislikedGenres
                List.of(),               // favouriteDirectors
                List.of(),               // favouriteActors
                List.of(),               // preferredDecades
                List.of(),               // preferredCountries
                List.of(),               // alreadyWatchedTitles
                List.of("Parasite", "Oldboy"), // highlyRatedTitles
                List.of(),               // poorlyRatedTitles
                null                     // aiSummary
        );
    }

    @Test
    void nostalgiaPromptIncludesMoodAndConstraint() {
        String prompt = builder.buildNostalgia(
                List.of("Parasite"), List.of("Oldboy"),
                "cozy", new MovieFilter(), 5);

        assertTrue(prompt.contains("cozy"));
        assertTrue(prompt.contains("Parasite"));
        assertTrue(prompt.contains("ALREADY SEEN"));   // the rewatch constraint
    }

}