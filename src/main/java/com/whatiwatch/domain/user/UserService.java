package com.whatiwatch.domain.user;

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

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    
}
