package com.whatiwatch.domain.user;

import java.time.LocalDateTime;

/*
- Represents a single entry in a user's watchlist
- Tracks what they want to watch, are watching or have watched
*/
public record  WatchListEntry(
    String id,
    String userId,
    int movieId,
    String movieTitle,
    Status status,
    LocalDateTime addedAt,
    LocalDateTime watchedAt
) {

   // Possible states of a watchlist entry
   public enum Status {
    WANT_TO_WATCH,
    WATCHING,
    WATCHED
   } 

   // Creates a new watchlist entry when a user saves a movie, starts at WANT_TO_WATCH
   public static WatchListEntry create(String userId, int movieId, String movieTitle) {
        return new WatchListEntry(
            java.util.UUID.randomUUID().toString(),
            userId, 
            movieId, 
            movieTitle, 
            Status.WANT_TO_WATCH, 
            LocalDateTime.now(), 
            null
        );
   }

   //Returns a copy marked as WATCHED, with the watched timestamp set.
   public WatchListEntry markAsWatched() {
        return new WatchListEntry(
            id, userId, movieId, movieTitle, 
            Status.WATCHED, 
            addedAt,
            LocalDateTime.now() 
        );
   }

   //Returns true if the user has already watched this movie.
   public boolean isWatched() {
        return status == Status.WATCHED;
   }

}
