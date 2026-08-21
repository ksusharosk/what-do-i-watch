package com.whatiwatch.api;

/**
 * Request body for adding or updating a watchlist entry.
 * `status` is optional on add (defaults to WANT_TO_WATCH); used to mark watched.
 */
public record WatchListRequest(
        int movieId,
        String movieTitle,
        String status   // "WANT_TO_WATCH", "WATCHING", "WATCHED", or null
) {}