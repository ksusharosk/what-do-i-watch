package com.whatiwatch.domain.user;

import java.util.Optional;
import java.util.List;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.whatiwatch.config.EncryptionService;
import com.whatiwatch.config.UnauthorizedException;
import com.whatiwatch.domain.user.User;
import com.whatiwatch.domain.user.UserEntity;
import com.whatiwatch.domain.user.UserRepository;
import com.whatiwatch.domain.user.UserPreferences;

import org.springframework.stereotype.Service;

import com.whatiwatch.domain.user.User;
import com.whatiwatch.domain.user.UserEntity;
import com.whatiwatch.domain.user.UserRepository;

/**
 * Application-level user operations. Bridges Google OAuth identities to
 * persisted users: on login we either load the existing user or create one.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public UserService(UserRepository userRepository, EncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    /**
     * Finds the user with this Google ID, or creates and saves a new one
     * 
     * @param googleId the user's Google account ID
     * @param email their email
     * @param displayName their display name
     * @return the existing or newly-created user
     */
    public User findOrCreate(String googleId, String email, String displayName) {
        return userRepository.findByGoogleId(googleId)
            .map(UserEntity::toDomain)
            .orElseGet(() -> createUser(googleId, email, displayName));
    }

    private User createUser(String googleId, String email, String displayName) {
        User newUser = User.newUser(googleId, email, displayName);
        userRepository.save(UserEntity.fromDomain(newUser));
        return newUser;
    }

    /**
     * Loads the domain User for a logged-in Google account
     * 
     * @param googleId the "sub" claim from the OIDC user
     * @return the user, if one exists
     */
    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId)
            .map(UserEntity::toDomain);
    }

    /**
     * Returns the domain User for the logged-in principal, or throws if there
     * isn't one. Use in controllers for endpoints that require authentication.
     *
     * @param oidcUser the authenticated principal (may be null for guests)
     * @throws UnauthorizedException if no user is logged in or found
     */
    public User requireUser(OidcUser oidcUser) {
        if (oidcUser == null) {
            throw new UnauthorizedException("Login required");
        }
        return findByGoogleId(oidcUser.getSubject())
                .orElseThrow(() -> new UnauthorizedException("No user for the authenticated principal"));
    }

    /**
     * Updates the given user's preferences (partial — only non-null fields
     * are changed) and persists the change.
     */
    public User updatePreferences(User user,
                                  List<Integer> preferredGenreIds,
                                  List<Integer> excludedGenreIds,
                                  List<String> preferredDecades,
                                  List<String> preferredCountries,
                                  String preferredLanguage,
                                  String aiBackend) {
        UserPreferences current = user.preferences();

        UserPreferences updated = new UserPreferences(
                preferredGenreIds != null ? preferredGenreIds : current.preferredGenreIds(),
                excludedGenreIds != null ? excludedGenreIds : current.excludedGenreIds(),
                preferredDecades != null ? preferredDecades : current.preferredDecades(),
                preferredCountries != null ? preferredCountries : current.preferredCountries(),
                preferredLanguage != null ? preferredLanguage : current.preferredLanguage(),
                current.favouriteActors(),      // derived, not user-set
                current.favouriteDirectors(),   // derived, not user-set
                aiBackend != null ? aiBackend : current.aiBackend(),
                current.encryptedApiKey()       // handled separately (sensitive)
        );

        User updatedUser = user.withPreferences(updated);
        userRepository.save(UserEntity.fromDomain(updatedUser));
        return updatedUser;
    }

    /**
     * Sets the user's AI backend choice and their API key, 
     * which is encrypted before storage
     */
    public User setApiKey(User user, String aiBackend, String plaintextApiKey) {
        String encrypted = encryptionService.encrypt(plaintextApiKey);

        UserPreferences current = user.preferences();
        UserPreferences updated = current.withAiBackend(
            aiBackend != null ? aiBackend : current.aiBackend(),
            encrypted);
        
        User updatedUser = user.withPreferences(updated);
        userRepository.save(UserEntity.fromDomain(updatedUser));
        return updatedUser;
    }
    
}
