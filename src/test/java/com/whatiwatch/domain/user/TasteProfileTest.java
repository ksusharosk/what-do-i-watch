package com.whatiwatch.domain.user;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class TasteProfileTest {
    
    @Test
    void emptyProfileHasNoData() {
        TasteProfile profile = TasteProfile.empty("user1");
        assertFalse(profile.hasEnoughData());
    }

    @Test
    void profileWithHighlyRatedTitlesHasData() {
        TasteProfile profile = new TasteProfile(
            "user1",
            List.of("Thriller"),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of("Parasite", "Oldboy"),
            List.of(),
            null
        );
        assertTrue(profile.hasEnoughData());
    }

    @Test
    void newUserGetsGenericContext() {
        TasteProfile profile = TasteProfile.empty("user1");
        String context = profile.toPromptContext();
        assertTrue(context.contains("New user"));
    }

    @Test
    void promptContextIncludesLikedGenres() {
        TasteProfile profile = new TasteProfile(
            "user1",
            List.of("Thriller", "Drama"),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of("Parasite"),
            List.of(),
            null
        );
        String context = profile.toPromptContext();
        assertTrue(context.contains("Thriller"));
        assertTrue(context.contains("Drama"));
    }

    @Test
    void promptContextIncludesAlreadyWatched() {
        TasteProfile profile = new TasteProfile(
            "user1",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of("Inception", "Parasite"),
            List.of("Inception"),
            null
        );
        String context = profile.toPromptContext();
        assertTrue(context.contains("Inception"));
        assertTrue(context.contains("Parasite"));
    }

    @Test
    void promptContextIncludesPoorlyRatedTitles() {
        TasteProfile profile = new TasteProfile(
            "user1",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of("Parasite"),
            List.of("Transformers"),
            null
        );
        String context = profile.toPromptContext();
        assertTrue(context.contains("Transformers"));
    }

}
