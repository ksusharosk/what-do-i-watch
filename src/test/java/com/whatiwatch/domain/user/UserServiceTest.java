package com.whatiwatch.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.whatiwatch.domain.user.User;
import com.whatiwatch.domain.user.UserEntity;
import com.whatiwatch.domain.user.UserRepository;

class UserServiceTest {

    private UserRepository repo;
    private UserService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(UserRepository.class);
        service = new UserService(repo);
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
        assertEquals("ollama", updated.preferences().aiBackend());
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
}