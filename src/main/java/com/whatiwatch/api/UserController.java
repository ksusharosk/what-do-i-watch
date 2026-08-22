package com.whatiwatch.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.whatiwatch.domain.user.User;
import com.whatiwatch.domain.user.UserService;

/**
 * Endpoints about the currently logged-in user
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns the loggen-in user's profile. Requires authentication
     * (guests get a 401 from the security config)
     */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.requireUser(oidcUser);
        return UserResponse.from(user);
    }
    
}
