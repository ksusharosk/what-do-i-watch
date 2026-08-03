package com.whatiwatch.domain.user;

import java.time.LocalDateTime;

/*
- Represents a logged-in user of the app
- Created authomatically when someone first logs in via Google OAuth
- use wuthPreferences() to create updated copy
*/
public record User(
    String id,
    String email,
    String displayName,
    String googleId,
    LocalDateTime createdAt,
    UserPreferences preferences
) {
    /*
    - Creates a new user with default preferences
    - Called the first time someone logs in via Google
    */
    public static User newUser(String googleId, String email, String displayName) {
        return new User(
            java.util.UUID.randomUUID().toString(),
            email,
            displayName,
            googleId,
            LocalDateTime.now(),
            UserPreferences.defaults()
        );
    
    }

    //Returns a copy of this user with updated preferences
    public User withPreferences(UserPreferences preferences) {
        return new User(id, email, displayName, googleId, createdAt, preferences);
    }

}