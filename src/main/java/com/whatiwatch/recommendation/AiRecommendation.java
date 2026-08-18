package com.whatiwatch.recommendation;

/**
 - A single recommendation as returned by AI, before enrichment.
    @param title the movie title the AI suggested
    @param year the release year (0 if AI didn't give one)
    @param pitch one-sentence explanation of why the user would like it
*/

public record AiRecommendation( String title, int year, String pitch) {

    public AiRecommendation {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be null or blank");
        }
        if (pitch == null || pitch.isBlank()) {
            throw new IllegalArgumentException("pitch cannot be null or blank");
        }
        if ( year < 0) {
            throw new IllegalArgumentException("year cannot be negative");
        }
    }

}
