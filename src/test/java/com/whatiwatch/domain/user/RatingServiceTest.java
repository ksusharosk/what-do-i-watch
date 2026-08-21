package com.whatiwatch.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.whatiwatch.domain.user.MovieRating;
import com.whatiwatch.domain.user.MovieRatingEntity;
import com.whatiwatch.domain.user.MovieRatingRepository;

class RatingServiceTest {

    private MovieRatingRepository repo;
    private RatingService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(MovieRatingRepository.class);
        service = new RatingService(repo);
    }

    @Test
    void ratingANewMovieSavesIt() {
        // No existing ratings for this user.
        when(repo.findByUserIdAndMovieId("user1", 550)).thenReturn(Optional.empty());


        MovieRating rating = service.rate("user1", 550, "Fight Club", 9, "Great");

        assertEquals(550, rating.movieId());
        assertEquals(9, rating.rating());
        // Verify a save actually happened.
        verify(repo).save(any(MovieRatingEntity.class));
    }

    @Test
    void ratingExistingMovieUpdatesIt() {
        // The user already rated movie 550 a 7.
        MovieRating existing = MovieRating.create("user1", 550, "Fight Club", 7, null);
        MovieRatingEntity existingEntity = MovieRatingEntity.fromDomain(existing);
        when(repo.findByUserIdAndMovieId("user1", 550)).thenReturn(Optional.of(existingEntity));


        MovieRating result = service.rate("user1", 550, "Fight Club", 9, "Changed my mind");

        assertEquals(9, result.rating());   // updated to new value
        verify(repo).save(existingEntity);  // saved the existing entity, not a new one
    }

    @Test
    void getRatingsReturnsUsersRatings() {
        MovieRating r = MovieRating.create("user1", 550, "Fight Club", 9, null);
        when(repo.findByUserId("user1"))
                .thenReturn(List.of(MovieRatingEntity.fromDomain(r)));

        List<MovieRating> ratings = service.getRatings("user1");

        assertEquals(1, ratings.size());
        assertEquals(550, ratings.get(0).movieId());
    }

    @Test
    void invalidRatingIsRejected() {
        when(repo.findByUserIdAndMovieId("user1", 550)).thenReturn(Optional.empty());

        // MovieRating.create enforces 1–10, so 15 throws.
        assertThrows(IllegalArgumentException.class,
                () -> service.rate("user1", 550, "Fight Club", 15, null));

        // And nothing was saved.
        verify(repo, never()).save(any());
    }

    @Test
    void deleteRemovesExistingRating() {
        MovieRating r = MovieRating.create("user1", 550, "Fight Club", 9, null);
        MovieRatingEntity entity = MovieRatingEntity.fromDomain(r);
        when(repo.findByUserIdAndMovieId("user1", 550)).thenReturn(Optional.of(entity));

        service.deleteRating("user1", 550);

        verify(repo).delete(entity);
    }

    @Test
    void deleteDoesNothingWhenRatingAbsent() {
        when(repo.findByUserIdAndMovieId("user1", 550)).thenReturn(Optional.empty());


        service.deleteRating("user1", 999);

        verify(repo, never()).delete(any());
    }
}