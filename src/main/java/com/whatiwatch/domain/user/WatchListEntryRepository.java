package com.whatiwatch.domain.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

// Data-access interface for watchlist entries
public interface WatchListEntryRepository extends JpaRepository<WatchListEntryEntity, String> {
    
    // All watchlist entries for a user
    List<WatchListEntryEntity> findByUserId(String userId);

    // A user's entires filtered by status, e.g only watched films
    List<WatchListEntryEntity> findByUserIdAndStatus(String userId, WatchListEntry.Status status);

}
