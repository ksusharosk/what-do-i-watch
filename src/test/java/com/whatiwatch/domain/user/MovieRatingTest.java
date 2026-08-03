package com.whatiwatch.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class MovieRatingTest {
    
    @Test
    void validRatingCreatedSuccessfully() {
        MovieRating rating = MovieRating.create("user1", 1, "Parasite", 9, "Loved it");
        assertEquals(9, rating.rating());
        assertEquals("Parasite", rating.movieTitle());
    }

    @Test
    void ratingBelowMinThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> MovieRating.create("user1", 1, "Parasite", 0, null)
        );
    }

    @Test
    void ratingAboveMaxThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> MovieRating.create("user1", 1, "Parasite", 11, null)
        );
    }    

    @Test
    void highRatingPositive() {
        MovieRating rating = MovieRating.create("user1", 1, "Parasite", 8, null);
        assertTrue(rating.isPositive());
        assertFalse(rating.isNegative());
    }

    @Test
    void lowRatingIsNegative() {
        MovieRating rating = MovieRating.create("user1", 1, "Parasite", 3, null);
        assertTrue(rating.isNegative());
        assertFalse(rating.isPositive());
    }

    @Test
    void middleRatingIsNeitherPositiveNorNegative() {
        MovieRating rating = MovieRating.create("user1", 1, "Parasite", 5, null);
        assertFalse(rating.isPositive());
        assertFalse(rating.isNegative());
    }

}
