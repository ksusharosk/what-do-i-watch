package com.whatiwatch.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.whatiwatch.domain.user.User;
import com.whatiwatch.domain.user.UserPreferences;
import com.whatiwatch.domain.user.UserService;

@RestController
@RequestMapping("/api/preferences")
public class PreferencesController {

    private final UserService userService;

    public PreferencesController(UserService userService) {
        this.userService = userService;
    }

    /** Get the logged-in user's preferences. */
    @GetMapping
    public UserPreferences getPreferences(@AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.requireUser(oidcUser);
        return user.preferences();
    }

    /** Update the logged-in user's preferences (partial update). */
    @PutMapping
    public UserPreferences updatePreferences(@RequestBody PreferencesRequest request,
                                             @AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.requireUser(oidcUser);
        User updated = userService.updatePreferences(user,
                request.preferredGenreIds(),
                request.excludedGenreIds(),
                request.preferredDecades(),
                request.preferredCountries(),
                request.preferredLanguage(),
                request.aiBackend());
        return updated.preferences();
    }
}