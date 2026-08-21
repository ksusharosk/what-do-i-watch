package com.whatiwatch.domain.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// Data-access interface for movie ratings
public interface MovieRatingRepository extends JpaRepository<MovieRatingEntity, String> {
    
    // All ratings by a given user - the core query for building a taste profile
    List<MovieRatingEntity> findByUserId(String userId);

    Optional<MovieRatingEntity> findByUserIdAndMovieId(String userId, int movieId);
}
