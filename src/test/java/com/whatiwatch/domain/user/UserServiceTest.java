package com.whatiwatch.domain.user;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.whatiwatch.config.EncryptionService;
import com.whatiwatch.domain.user.User;
import com.whatiwatch.domain.user.UserEntity;
import com.whatiwatch.domain.user.UserRepository;

class UserServiceTest {

    private UserRepository repo;
    private UserService service;
    private EncryptionService encryptionService;
    private RatingService ratingService;
    private WatchListService watchListService;
    private RecommendationHistoryService historyService;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(UserRepository.class);
        encryptionService = Mockito.mock(EncryptionService.class);
        ratingService = Mockito.mock(RatingService.class);
        watchListService = Mockito.mock(WatchListService.class);
        historyService = Mockito.mock(RecommendationHistoryService.class);
        service = new UserService(repo, encryptionService, ratingService, watchListService, historyService);
    }

    @Test
    void findOrCreateReturnsExistingUser() {
        User existing = User.newUser("google123", "a@example.com", "Alice");
        when(repo.findByGoogleId("google123"))
                .thenReturn(Optional.of(UserEntity.fromDomain(existing)));

        User result = service.findOrCreate("google123", "a@example.com", "Alice");

        assertEquals(existing.id(), result.id());
        // Existing user found → no new save.
        verify(repo, Mockito.never()).save(any());
    }

    @Test
    void findOrCreateCreatesNewUser() {
        when(repo.findByGoogleId("newguy")).thenReturn(Optional.empty());

        User result = service.findOrCreate("newguy", "new@example.com", "Newbie");

        assertEquals("newguy", result.googleId());
        assertEquals("new@example.com", result.email());
        verify(repo).save(any(UserEntity.class));   // new user saved
    }

    @Test
    void updatePreferencesChangesOnlyProvidedFields() {
        User user = User.newUser("google123", "a@example.com", "Alice");
        // Defaults: language "en", backend "ollama", empty genre lists.

        User updated = service.updatePreferences(user,
                List.of(28, 53),   // preferredGenreIds
                null,              // excludedGenreIds — unchanged
                null,              // preferredDecades — unchanged
                null,              // preferredCountries — unchanged
                "fr",              // preferredLanguage — changed
                null);             // aiBackend — unchanged

        assertEquals(List.of(28, 53), updated.preferences().preferredGenreIds());
        assertEquals("fr", updated.preferences().preferredLanguage());
        // Untouched fields keep their original values:
        assertEquals("groq", updated.preferences().aiBackend());
        assertEquals(List.of(), updated.preferences().excludedGenreIds());

        verify(repo).save(any(UserEntity.class));
    }

    @Test
    void updatePreferencesPreservesEncryptedApiKey() {
        User user = User.newUser("google123", "a@example.com", "Alice");

        User updated = service.updatePreferences(user,
                null, null, null, null, "fr", null);

        // The (currently null) API key and derived fields are carried over untouched.
        assertEquals(user.preferences().encryptedApiKey(),
                updated.preferences().encryptedApiKey());
        assertEquals(user.preferences().favouriteActors(),
                updated.preferences().favouriteActors());
    }

        @Test
    void setApiKeyEncryptsAndStores() {
        User user = User.newUser("google123", "a@example.com", "Alice");

        // Stub the encryptor: plaintext "gsk_raw" -> "ENCRYPTED".
        when(encryptionService.encrypt("gsk_raw")).thenReturn("ENCRYPTED");

        User updated = service.setApiKey(user, "groq", "gsk_raw");

        // The stored key is the encrypted value, not the plaintext.
        assertEquals("ENCRYPTED", updated.preferences().encryptedApiKey());
        assertEquals("groq", updated.preferences().aiBackend());
        verify(encryptionService).encrypt("gsk_raw");   // encryption was invoked
        verify(repo).save(any(UserEntity.class));       // and it was persisted
    }

    @Test
    void setApiKeyWithNullKeyClearsIt() {
        User user = User.newUser("google123", "a@example.com", "Alice");
        when(encryptionService.encrypt(null)).thenReturn(null);

        User updated = service.setApiKey(user, "ollama", null);

        assertNull(updated.preferences().encryptedApiKey());
        verify(repo).save(any(UserEntity.class));
    }

    @Test
    void updateDisplayNameChangesTheName() {
        User user = User.newUser("google123", "a@example.com", "OldName");

        User updated = service.updateDisplayName(user, "NewName");

        assertEquals("NewName", updated.displayName());
        verify(repo).save(any(UserEntity.class));
    }

    @Test
    void updateDisplayNameRejectsBlank() {
        User user = User.newUser("google123", "a@example.com", "OldName");

        assertThrows(IllegalArgumentException.class,
                () -> service.updateDisplayName(user, "  "));

        verify(repo, never()).save(any());
    }
    
    @Test
    void deleteAccountRemovesRatingsWatchlistAndUser() {
        User user = User.newUser("google123", "a@example.com", "Alice");
        // The user exists in the repo (so the delete path finds them).
        when(repo.findByGoogleId("google123"))
                .thenReturn(Optional.of(UserEntity.fromDomain(user)));

        service.deleteAccount(user);

        // All three deletions happened...
        verify(ratingService).deleteAllForUser(user.id());
        verify(watchListService).deleteAllForUser(user.id());
        verify(repo).delete(any(UserEntity.class));
    }

    @Test
    void deleteAccountStillClearsDataWhenUserRecordAbsent() {
        User user = User.newUser("google123", "a@example.com", "Alice");
        // User record already gone from the repo.
        when(repo.findByGoogleId("google123")).thenReturn(Optional.empty());

        service.deleteAccount(user);

        // Ratings/watchlist clearing still runs; user delete is simply skipped.
        verify(ratingService).deleteAllForUser(user.id());
        verify(watchListService).deleteAllForUser(user.id());
        verify(repo, never()).delete(any());
    }

        @Test
    void completeOnboardingSetsTheFlag() {
        User user = User.newUser("google123", "a@example.com", "Alice");
        assertFalse(user.preferences().hasCompletedOnboarding());   // starts false

        User updated = service.completeOnboarding(user);

        assertTrue(updated.preferences().hasCompletedOnboarding());
        verify(repo).save(any(UserEntity.class));
    }

    @Test
    void setAvatarAcceptsValidId() {
        User user = User.newUser("google123", "a@example.com", "Alice");

        User updated = service.setAvatar(user, "AVATAR_2");

        assertEquals("AVATAR_2", updated.preferences().avatarId());
        verify(repo).save(any(UserEntity.class));
    }

    @Test
    void setAvatarRejectsUnknownId() {
        User user = User.newUser("google123", "a@example.com", "Alice");

        assertThrows(IllegalArgumentException.class,
                () -> service.setAvatar(user, "not_a_real_avatar"));

        verify(repo, never()).save(any());
    }
}