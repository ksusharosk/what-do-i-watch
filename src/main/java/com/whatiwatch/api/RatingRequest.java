package com.whatiwatch.api;

/**
 * Request body for rating a movie.
 */
public record RatingRequest(
        int movieId,
        String movieTitle,
        int rating,
        String review
) {}