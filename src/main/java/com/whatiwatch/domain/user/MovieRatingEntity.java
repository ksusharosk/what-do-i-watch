package com.whatiwatch.domain.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * JPA entity for persisting movie ratings, mapped to the 'movie_ratings' table
 * Mutable counterpart to the immutable MovieRating record
 */
@Entity
@Table(name = "movie_ratings", indexes = {
    @Index(name = "idx_rating_user", columnList = "userId")
})
public class MovieRatingEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private int movieId;

    private String movieTitle;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "text")
    private String review;

    @Column(nullable = false)
    private LocalDateTime ratedAt;

    protected MovieRatingEntity() {
    }

    public MovieRatingEntity(String id, String userId, int movieId, String movieTitle,
                            int rating, String review, LocalDateTime ratedAt) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.rating = rating;
        this.review = review;
        this.ratedAt = ratedAt;
    }

    public static MovieRatingEntity fromDomain(MovieRating rating) {
        return new MovieRatingEntity(
            rating.id(),
            rating.userId(),
            rating.movieId(),
            rating.movieTitle(),
            rating.rating(),
            rating.review(),
            rating.ratedAt());
    }

    public MovieRating toDomain() {
        return new MovieRating(id, userId, movieId, movieTitle, rating, review, ratedAt);
    }

    // Getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public LocalDateTime getRatedAt() { return ratedAt; }
    public void setRatedAt(LocalDateTime ratedAt) { this.ratedAt = ratedAt; }
    
}
