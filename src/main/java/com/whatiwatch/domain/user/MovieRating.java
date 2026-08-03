package com.whatiwatch.domain.user;

import java.time.LocalDateTime;

/*
- Represents a user's rating of a movie
- Ratings feed into the taste profile to improve recommendations over time
*/
public record MovieRating(
    String id, 
    String userId,
    int movieId,
    String movieTitle,
    int rating,
    String review,
    LocalDateTime ratedAt
) {
    // Valid rating are between 1 and 10
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 10;

    /*
    - Creates a new movie rating
    - Validates that the rating is within the allowed range
    */
   public static MovieRating create(
        String userId,
        int movieId,
        String movieTitle,
        int rating,
        String review
   ) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException(
                "Rating must be between " + MIN_RATING +
                " and " + MAX_RATING + ", got: " + rating
            );
        }

        return new MovieRating(
            java.util.UUID.randomUUID().toString(),
            userId, 
            movieId, 
            movieTitle, 
            rating, 
            review, 
            LocalDateTime.now()
        );
    }
    
    //Returns true if this is a positive rating (8 or above)
    public boolean isPositive() {
        return rating >= 8;
    }

    // Returns if this is a negative rating (4 or below)
    public boolean isNegative() {
        return rating <= 4;
    }

}
