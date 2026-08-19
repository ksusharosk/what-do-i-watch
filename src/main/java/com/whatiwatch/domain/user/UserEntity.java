package com.whatiwatch.domain.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity for persisting users, mapped to the 'users' table
 * Mutable counterpart to the immutable User record
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String googleId;

    @Column(nullable = false)
    private String email;

    private String displayName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Stored as a JSON string via the converter; PostgreSQL holds it in a text column
    @Convert(converter = UserPreferencesConverter.class)
    @Column(columnDefinition = "text")
    private UserPreferences preferences;

    protected UserEntity() {
    }

    public UserEntity(String id, String googleId, String email,
                    String displayName, LocalDateTime createdAt,
                    UserPreferences preferences) {
        this.id = id;
        this.googleId = googleId;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = createdAt;
        this.preferences = preferences;
    }

    // --- Conversion to/from the domain record ---

    // Builds an entity from a domain User (for saving)
    public static UserEntity fromDomain(User user) {
        return new UserEntity(
            user.id(),
            user.googleId(),
            user.email(),
            user.displayName(),
            user.createdAt(),
            user.preferences());
    }

    // Converts this entity back to a domain User 
    public User toDomain() {
        return new User(id, email, displayName, googleId, createdAt, preferences);
    }

    // --- Getters / setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserPreferences getPreferences() { return preferences; }
    public void setPreferences(UserPreferences preferences) { this.preferences = preferences; }
    
}
