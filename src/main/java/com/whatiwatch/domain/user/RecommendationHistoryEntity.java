package com.whatiwatch.domain.user;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation_history", indexes = {
    @Index(name = "idx_history_user", columnList = "userId")
})
public class RecommendationHistoryEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String mood;

    private String decade;

    // genreIds and films both stored as JSON text via converters.
    @Convert(converter = IntListConverter.class)
    @Column(columnDefinition = "text")
    private List<Integer> genreIds;

    @Convert(converter = RecommendedFilmsConverter.class)
    @Column(columnDefinition = "text")
    private List<RecommendationHistoryEntry.RecommendedFilm> films;

    protected RecommendationHistoryEntity() {}

    public RecommendationHistoryEntity(String id, String userId, LocalDateTime createdAt,
                                       String mood, String decade,
                                       List<Integer> genreIds,
                                       List<RecommendationHistoryEntry.RecommendedFilm> films) {
        this.id = id;
        this.userId = userId;
        this.createdAt = createdAt;
        this.mood = mood;
        this.decade = decade;
        this.genreIds = genreIds;
        this.films = films;
    }

    public static RecommendationHistoryEntity fromDomain(RecommendationHistoryEntry e) {
        return new RecommendationHistoryEntity(
                e.id(), e.userId(), e.createdAt(), e.mood(), e.decade(),
                e.genreIds(), e.films());
    }

    public RecommendationHistoryEntry toDomain() {
        return new RecommendationHistoryEntry(
                id, userId, createdAt, mood, genreIds, decade, films);
    }

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public String getDecade() { return decade; }
    public void setDecade(String decade) { this.decade = decade; }
    public List<Integer> getGenreIds() { return genreIds; }
    public void setGenreIds(List<Integer> genreIds) { this.genreIds = genreIds; }
    public List<RecommendationHistoryEntry.RecommendedFilm> getFilms() { return films; }
    public void setFilms(List<RecommendationHistoryEntry.RecommendedFilm> films) { this.films = films; }
}
