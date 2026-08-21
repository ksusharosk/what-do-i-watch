package com.whatiwatch.domain.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.whatiwatch.domain.user.MovieRating;
import com.whatiwatch.domain.user.MovieRatingEntity;
import com.whatiwatch.domain.user.MovieRatingRepository;

/**
 * Manages a user's movie ratings.
 */
@Service
public class RatingService {

    private final MovieRatingRepository ratingRepository;

    public RatingService(MovieRatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    /** All ratings for a user, as domain records. */
    public List<MovieRating> getRatings(String userId) {
        return ratingRepository.findByUserId(userId).stream()
                .map(MovieRatingEntity::toDomain)
                .toList();
    }

    /**
     * Rates a movie for a user. If the user has already rated this movie,
     * the existing rating is updated; otherwise a new one is created.
     */
    public MovieRating rate(String userId, int movieId, String movieTitle,
                            int rating, String review) {
        // Look for an existing rating of this movie by this user.
        MovieRatingEntity existing = ratingRepository
                .findByUserIdAndMovieId(userId, movieId)
                .orElse(null);

        if (existing != null) {
            existing.setRating(rating);
            existing.setReview(review);
            ratingRepository.save(existing);
            return existing.toDomain();
        }

        // MovieRating.create validates the 1–10 range and generates id/timestamp.
        MovieRating newRating = MovieRating.create(userId, movieId, movieTitle, rating, review);
        ratingRepository.save(MovieRatingEntity.fromDomain(newRating));
        return newRating;
    }

    /** Removes a user's rating of a movie, if present. */
    public void deleteRating(String userId, int movieId) {
        ratingRepository.findByUserIdAndMovieId(userId, movieId)
                .ifPresent(ratingRepository::delete);
    }
}