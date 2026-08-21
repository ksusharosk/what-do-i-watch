package com.whatiwatch.domain.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * JPA entity for persisting watchlist entries, mapped to the 'watchlist_entires'
 * Mutable counterpart to the immutable WatchListEntry record
 */
@Entity
@Table(name = "watchlist_entries", indexes = {
    @Index(name = "idx_watchlist_user", columnList = "userId")
})
public class WatchListEntryEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private int movieId;

    private String movieTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WatchListEntry.Status status;

    @Column(nullable = false)
    private LocalDateTime addedAt;

    private LocalDateTime watchedAt; // null until watched

    protected WatchListEntryEntity() {
    }

    public WatchListEntryEntity(String id, String userId, int movieId, String movieTitle,
                                WatchListEntry.Status status,
                                LocalDateTime addedAt, LocalDateTime watchedAt) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.status = status;
        this.addedAt = addedAt;
        this.watchedAt = watchedAt;
    } 

    public static WatchListEntryEntity fromDomain(WatchListEntry entry) {
        return new WatchListEntryEntity(
                entry.id(),
                entry.userId(),
                entry.movieId(),
                entry.movieTitle(),
                entry.status(),
                entry.addedAt(),
                entry.watchedAt());
    }

    public WatchListEntry toDomain() {
        return new WatchListEntry(id, userId, movieId, movieTitle, status, addedAt, watchedAt);
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

    public WatchListEntry.Status getStatus() { return status; }
    public void setStatus(WatchListEntry.Status status) { this.status = status; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public LocalDateTime getWatchedAt() { return watchedAt; }
    public void setWatchedAt(LocalDateTime watchedAt) { this.watchedAt = watchedAt; }
    
}
