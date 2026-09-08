package com.whatiwatch.domain.user;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A record of one recommendation session: what user asked for, and
 * what films were suggested. Stired per user for the history panel
 */
public record RecommendationHistoryEntry(
    String id,
    String userId,
    LocalDateTime createdAt,
    String mood, 
    List<Integer> genreIds,
    String decade,
    List<RecommendedFilm> films
) {
    /** A single suggested film within a history entry (lightweight) */
    public record RecommendedFilm(
        int movieId,
        String title,
        int year,
        String posterPath,
        String pitch
    ) {}

    /** Factory: builds an entry with a generated id and timestamp */
    public static RecommendationHistoryEntry create(String userId, String mood,
                                                    List<Integer> genreIds, String decade,
                                                    List<RecommendedFilm> films) {
        return new RecommendationHistoryEntry(
            java.util.UUID.randomUUID().toString(),
            userId, 
            LocalDateTime.now(), 
            mood, genreIds, decade, films);
    }

}
