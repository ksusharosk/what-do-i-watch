package com.whatiwatch.domain.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.whatiwatch.domain.user.WatchListEntry;
import com.whatiwatch.domain.user.WatchListEntryEntity;
import com.whatiwatch.domain.user.WatchListEntryRepository;

/**
 * Manages a user's watchlist.
 */
@Service
public class WatchListService {

    private final WatchListEntryRepository watchlistRepository;

    public WatchListService(WatchListEntryRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    /** All watchlist entries for a user. */
    public List<WatchListEntry> getWatchList(String userId) {
        return watchlistRepository.findByUserId(userId).stream()
                .map(WatchListEntryEntity::toDomain)
                .toList();
    }

    /**
     * Adds a movie to the watchlist, or updates its status if already present.
     * A null/blank status on a new entry defaults to WANT_TO_WATCH.
     */
    public WatchListEntry addOrUpdate(String userId, int movieId, String movieTitle, String status) {
        WatchListEntry.Status newStatus = parseStatus(status);

        WatchListEntryEntity existing = watchlistRepository
                .findByUserIdAndMovieId(userId, movieId)
                .orElse(null);

        if (existing != null) {
            existing.setStatus(newStatus);
            // Set the watched timestamp when moving to WATCHED (if not already set).
            if (newStatus == WatchListEntry.Status.WATCHED && existing.getWatchedAt() == null) {
                existing.setWatchedAt(java.time.LocalDateTime.now());
            }
            watchlistRepository.save(existing);
            return existing.toDomain();
        }

        // New entry. create() starts as WANT_TO_WATCH; if the caller asked for
        // WATCHED, mark it so (which also sets the watched timestamp).
        WatchListEntry entry = WatchListEntry.create(userId, movieId, movieTitle);
        if (newStatus == WatchListEntry.Status.WATCHED) {
            entry = entry.markAsWatched();
        } else if (newStatus == WatchListEntry.Status.WATCHING) {
            entry = entry.markAsWatching();
        }
        watchlistRepository.save(WatchListEntryEntity.fromDomain(entry));
        return entry;
    }

    /** Removes a movie from the watchlist, if present. */
    public void remove(String userId, int movieId) {
        watchlistRepository.findByUserIdAndMovieId(userId, movieId)
                .ifPresent(watchlistRepository::delete);
    }

    private WatchListEntry.Status parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return WatchListEntry.Status.WANT_TO_WATCH;
        }
        try {
            return WatchListEntry.Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status +
                    " (expected WANT_TO_WATCH, WATCHING, or WATCHED)");
        }
    }
}